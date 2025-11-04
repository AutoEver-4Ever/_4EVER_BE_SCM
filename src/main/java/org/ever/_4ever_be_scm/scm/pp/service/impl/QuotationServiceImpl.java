package org.ever._4ever_be_scm.scm.pp.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ever._4ever_be_scm.scm.iv.entity.Product;
import org.ever._4ever_be_scm.scm.iv.entity.ProductStock;
import org.ever._4ever_be_scm.scm.iv.repository.ProductRepository;
import org.ever._4ever_be_scm.scm.iv.repository.ProductStockRepository;
import org.ever._4ever_be_scm.scm.iv.service.StockReservationService;
import org.ever._4ever_be_scm.scm.pp.dto.*;
import org.ever._4ever_be_scm.scm.pp.entity.*;
import org.ever._4ever_be_scm.scm.pp.integration.dto.BusinessQuotationDto;
import org.ever._4ever_be_scm.scm.pp.integration.dto.BusinessQuotationListResponseDto;
import org.ever._4ever_be_scm.scm.pp.integration.port.BusinessQuotationServicePort;
import org.ever._4ever_be_scm.scm.pp.repository.*;
import org.ever._4ever_be_scm.scm.pp.service.QuotationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuotationServiceImpl implements QuotationService {

    private final BomRepository bomRepository;
    private final BomItemRepository bomItemRepository;
    private final ProductRepository productRepository;
    private final ProductStockRepository productStockRepository;
    private final MpsRepository mpsRepository;
    private final MpsDetailRepository mpsDetailRepository;
    private final MrpRepository mrpRepository;
    private final MesRepository mesRepository;
    private final MesOperationLogRepository mesOperationLogRepository;
    private final RoutingRepository routingRepository;
    private final OperationRepository operationRepository;
    private final StockReservationService stockReservationService;
    private final BusinessQuotationServicePort businessQuotationServicePort;

    @Override
    @Transactional(readOnly = true)
    public QuotationGroupListResponseDto getQuotationList(
            String statusCode, 
            LocalDate startDate, 
            LocalDate endDate, 
            int page, 
            int size) {
        
        // Business 서비스에서 견적 목록 조회 (이미 그룹핑된 형태)
        BusinessQuotationListResponseDto businessResponse = businessQuotationServicePort
                .getQuotationList(statusCode, startDate, endDate, page, size);
        
        // Business 견적 데이터를 QuotationGroupDto로 변환
        List<QuotationGroupListResponseDto.QuotationGroupDto> quotationGroups = businessResponse.getContent().stream()
                .map(this::convertToQuotationGroupDto)
                .collect(Collectors.toList());
        
        // PageInfo 변환
        QuotationGroupListResponseDto.PageInfo pageInfo = QuotationGroupListResponseDto.PageInfo.builder()
                .number(businessResponse.getPageInfo().getNumber())
                .size(businessResponse.getPageInfo().getSize())
                .totalElements(businessResponse.getPageInfo().getTotalElements())
                .totalPages(businessResponse.getPageInfo().getTotalPages())
                .hasNext(businessResponse.getPageInfo().isHasNext())
                .build();
        
        return QuotationGroupListResponseDto.builder()
                .content(quotationGroups)
                .pageInfo(pageInfo)
                .build();
    }
    
    /**
     * Business 견적 DTO를 QuotationGroupDto로 변환
     */
    private QuotationGroupListResponseDto.QuotationGroupDto convertToQuotationGroupDto(BusinessQuotationDto businessQuotation) {
        // 각 아이템의 Product 정보 조회 및 변환
        List<QuotationGroupListResponseDto.QuotationItemDto> items = businessQuotation.getItems().stream()
                .map(this::convertToQuotationItemDto)
                .collect(Collectors.toList());
        
        return QuotationGroupListResponseDto.QuotationGroupDto.builder()
                .quotationId(businessQuotation.getQuotationId())
                .quotationNumber(businessQuotation.getQuotationNumber())
                .customerName(businessQuotation.getCustomerName())
                .requestDate(businessQuotation.getQuotationDate() != null ? 
                            businessQuotation.getQuotationDate() : businessQuotation.getRequestDate())
                .dueDate(businessQuotation.getDueDate())
                .statusCode(businessQuotation.getStatusCode())
                .availableStatus(businessQuotation.getAvailableStatus())
                .items(items)
                .build();
    }
    
    /**
     * Business 견적 아이템 DTO를 QuotationItemDto로 변환
     */
    private QuotationGroupListResponseDto.QuotationItemDto convertToQuotationItemDto(BusinessQuotationDto.BusinessQuotationItemDto businessItem) {
        // Product 정보 조회 (Business에서 제공하지 않는 정보만 조회)
        Product product = productRepository.findById(businessItem.getItemId()).orElse(null);
        
        return QuotationGroupListResponseDto.QuotationItemDto.builder()
                .productId(businessItem.getItemId())
                .productName(businessItem.getItemName() != null ? 
                            businessItem.getItemName() : 
                            (product != null ? product.getProductName() : "알 수 없는 제품"))
                .quantity(businessItem.getQuantity())
                .uomName(businessItem.getUomName() != null ? 
                         businessItem.getUomName() : 
                         (product != null ? product.getUnit() : "EA"))
                .unitPrice(businessItem.getUnitPrice() != null ? 
                          businessItem.getUnitPrice() : 
                          (product != null && product.getOriginPrice() != null ? 
                           product.getOriginPrice().intValue() : 0))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuotationSimulateResponseDto> simulateQuotations(QuotationSimulateRequestDto requestDto, Pageable pageable) {
        log.info("견적 시뮬레이션 시작: quotationIds={}", requestDto.getQuotationIds());
        
        List<QuotationSimulateResponseDto> allResults = new ArrayList<>();
        
        for (String quotationId : requestDto.getQuotationIds()) {
            try {
                // Business 서비스에서 견적 데이터 조회
                BusinessQuotationDto businessQuotation = businessQuotationServicePort.getQuotationById(quotationId);
                if (businessQuotation == null || businessQuotation.getItems().isEmpty()) {
                    log.warn("견적 데이터를 찾을 수 없습니다: quotationId={}", quotationId);
                    continue;
                }
                
                // 각 아이템별로 시뮬레이션 수행 (페이징을 위해 아이템별로 분리)
                for (BusinessQuotationDto.BusinessQuotationItemDto item : businessQuotation.getItems()) {
                    QuotationSimulateResponseDto result = simulateQuotationItem(quotationId, businessQuotation, item);
                    if (result != null) {
                        allResults.add(result);
                    }
                }
                
            } catch (Exception e) {
                log.error("견적 시뮬레이션 중 오류 발생: quotationId={}", quotationId, e);
            }
        }
        
        // 페이징 처리
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allResults.size());
        List<QuotationSimulateResponseDto> pagedResults = allResults.subList(start, end);
        
        return new PageImpl<>(pagedResults, pageable, allResults.size());
    }
    
    /**
     * 단일 아이템에 대한 시뮬레이션 수행
     */
    private QuotationSimulateResponseDto simulateQuotationItem(String quotationId, 
                                                             BusinessQuotationDto businessQuotation,
                                                             BusinessQuotationDto.BusinessQuotationItemDto item) {
        
        String productId = item.getItemId();
        Integer requestQuantity = item.getQuantity();
        LocalDate requestDueDate = LocalDate.parse(businessQuotation.getDueDate());
        LocalDateTime now = LocalDateTime.now();
        
        log.info("아이템 시뮬레이션: productId={}, 요청수량={}", productId, requestQuantity);
        
        // 해당 제품의 BOM 조회
        Optional<Bom> bomOpt = bomRepository.findByProductId(productId);
        if (bomOpt.isEmpty()) {
            log.warn("BOM을 찾을 수 없습니다: productId={}", productId);
            return null;
        }
        
        Bom bom = bomOpt.get();
        
        // 1. 완제품의 현재 재고량 확인
        Integer finishedGoodsStock = getActualAvailableStock(productId);
        log.info("완제품 재고: {}개", finishedGoodsStock);
        
        // 2. 부족분 계산 (요구사항: 완제품 재고는 그대로 사용, 부족분만 생산)
        Integer shortageQuantity = Math.max(0, requestQuantity - finishedGoodsStock);
        Integer availableFromStock = Math.min(requestQuantity, finishedGoodsStock);
        
        log.info("완제품 재고에서 공급 가능: {}개, 추가 생산 필요: {}개", availableFromStock, shortageQuantity);
        
        // 3. 부족분에 대한 BOM 기반 생산 가능성 확인 (요구사항: 원자재로만 생산 가능량 계산)
        List<QuotationSimulateResponseDto.ShortageDto> shortages = new ArrayList<>();
        boolean hasShortage = false;
        Integer maxDeliveryDays = 0;
        
        if (shortageQuantity > 0) {
            // BOM 아이템들 조회하여 부족분 생산에 필요한 자재 계산
            List<BomItem> bomItems = bomItemRepository.findByBomId(bom.getId());
            
            for (BomItem bomItem : bomItems) {
                ProductionRequirement requirement = calculateProductionRequirement(bomItem, shortageQuantity, new HashSet<>());
                
                if (requirement.hasShortage()) {
                    shortages.addAll(requirement.getShortages());
                    hasShortage = true;
                }
                
                maxDeliveryDays = Math.max(maxDeliveryDays, requirement.getMaxDeliveryDays());
            }
            
            // BOM 리드타임 고려
            if (bom.getLeadTime() != null) {
                maxDeliveryDays = Math.max(maxDeliveryDays, bom.getLeadTime().intValue());
            }
        }
        
        // 4. 최종 가용량 계산 (요구사항: 완제품 재고만 가용량으로 계산)
        Integer totalAvailableQuantity = availableFromStock;
        Integer finalShortageQuantity = shortageQuantity; // 부족분은 그대로
        
        // 5. 제안 납기 계산
        LocalDate suggestedDueDate = requestDueDate;
        if (hasShortage) {
            suggestedDueDate = requestDueDate.plusDays(maxDeliveryDays);
        }
        
        log.info("시뮬레이션 결과 - 요청:{}개, 재고가용:{}개, 부족:{}개", 
                requestQuantity, totalAvailableQuantity, finalShortageQuantity);
        
        return QuotationSimulateResponseDto.builder()
            .quotationId(quotationId)
            .quotationNumber(businessQuotation.getQuotationNumber())
            .customerCompanyId("1") // Business에서 제공하지 않아서 임시
            .customerCompanyName(businessQuotation.getCustomerName())
            .productId(productId)
            .productName(item.getItemName())
            .requestQuantity(requestQuantity)
            .requestDueDate(requestDueDate)
            .simulation(QuotationSimulateResponseDto.SimulationDto.builder()
                .status(hasShortage ? "FAIL" : "SUCCESS")
                .availableQuantity(totalAvailableQuantity)
                .shortageQuantity(finalShortageQuantity)
                .suggestedDueDate(suggestedDueDate)
                .generatedAt(now)
                .build())
            .shortages(shortages)
            .build();
    }

    /**
     * 제품의 실제 사용 가능한 재고량 조회 (예약재고 제외)
     */
    private Integer getActualAvailableStock(String productId) {
        Optional<ProductStock> productStockOpt = productStockRepository.findByProductId(productId);
        
        Integer actualAvailable = productStockOpt
            .map(stock -> stock.getActualAvailableCount() != null ? stock.getActualAvailableCount().intValue() : 0)
            .orElse(0);
        
        log.debug("실제 가용재고 조회: productId={}, 가용재고={}", productId, actualAvailable);
        return actualAvailable;
    }
    
    /**
     * 재귀적으로 생산 요구사항 계산 (BOM의 하위 BOM 포함)
     */
    private ProductionRequirement calculateProductionRequirement(BomItem bomItem, Integer requiredQuantity, Set<String> processedProducts) {
        ProductionRequirement requirement = new ProductionRequirement();
        
        // 순환 참조 방지
        if (processedProducts.contains(bomItem.getComponentId())) {
            log.warn("순환 참조 감지: productId={}", bomItem.getComponentId());
            return requirement;
        }
        
        processedProducts.add(bomItem.getComponentId());
        
        try {
            Product componentProduct = productRepository.findById(bomItem.getComponentId()).orElse(null);
            if (componentProduct == null) {
                log.warn("구성품목을 찾을 수 없습니다: componentId={}", bomItem.getComponentId());
                return requirement;
            }
            
            Integer totalRequired = bomItem.getCount().multiply(BigDecimal.valueOf(requiredQuantity)).intValue();
            
            if ("MATERIAL".equals(componentProduct.getCategory())) {
                // 원자재인 경우 - 직접 재고 확인
                Integer currentStock = getActualAvailableStock(bomItem.getComponentId());
                
                if (currentStock < totalRequired) {
                    Integer shortQuantity = totalRequired - currentStock;
                    requirement.addShortage(QuotationSimulateResponseDto.ShortageDto.builder()
                        .itemId(bomItem.getComponentId())
                        .itemName(componentProduct.getProductName())
                        .requiredQuantity(totalRequired)
                        .currentStock(currentStock)
                        .shortQuantity(shortQuantity)
                        .build());
                }
                
                // 이 원자재로 생산 가능한 수량 계산
                Integer maxProduction = bomItem.getCount().intValue() > 0 ? 
                                       currentStock / bomItem.getCount().intValue() : Integer.MAX_VALUE;
                requirement.updateMaxProductionCapacity(maxProduction);
                
                // 공급업체 배송 기간 고려
                if (componentProduct.getSupplierCompany() != null && 
                    componentProduct.getSupplierCompany().getDeliveryDays() != null) {
                    requirement.updateMaxDeliveryDays(componentProduct.getSupplierCompany().getDeliveryDays());
                }
                
            } else if ("ITEM".equals(componentProduct.getCategory())) {
                // 중간제품인 경우 - 재귀적으로 하위 BOM 확인
                Integer componentStock = getActualAvailableStock(bomItem.getComponentId());
                Integer componentShortage = Math.max(0, totalRequired - componentStock);
                
                if (componentShortage > 0) {
                    // 하위 BOM이 있는지 확인
                    Optional<Bom> subBomOpt = bomRepository.findByProductId(bomItem.getComponentId());
                    if (subBomOpt.isPresent()) {
                        List<BomItem> subBomItems = bomItemRepository.findByBomId(subBomOpt.get().getId());
                        
                        for (BomItem subBomItem : subBomItems) {
                            ProductionRequirement subRequirement = calculateProductionRequirement(
                                subBomItem, componentShortage, new HashSet<>(processedProducts));
                            
                            requirement.getShortages().addAll(subRequirement.getShortages());
                            if (subRequirement.hasShortage()) {
                                requirement.hasShortage = true;
                            }
                            requirement.updateMaxProductionCapacity(subRequirement.getMaxProductionCapacity());
                            requirement.updateMaxDeliveryDays(subRequirement.getMaxDeliveryDays());
                        }
                        
                        // 하위 BOM의 리드타임 고려
                        if (subBomOpt.get().getLeadTime() != null) {
                            requirement.updateMaxDeliveryDays(subBomOpt.get().getLeadTime().intValue());
                        }
                    } else {
                        // 하위 BOM이 없는 중간제품인 경우 부족분을 직접 계산
                        requirement.addShortage(QuotationSimulateResponseDto.ShortageDto.builder()
                            .itemId(bomItem.getComponentId())
                            .itemName(componentProduct.getProductName())
                            .requiredQuantity(totalRequired)
                            .currentStock(componentStock)
                            .shortQuantity(componentShortage)
                            .build());
                    }
                }
                
                // 중간제품의 총 가용량 계산 (재고 + 생산가능량)
                Integer totalAvailableComponent = componentStock + 
                    (requirement.getMaxProductionCapacity() == Integer.MAX_VALUE ? componentShortage : requirement.getMaxProductionCapacity());
                
                Integer maxProduction = bomItem.getCount().intValue() > 0 ? 
                                       totalAvailableComponent / bomItem.getCount().intValue() : Integer.MAX_VALUE;
                requirement.updateMaxProductionCapacity(maxProduction);
            }
            
        } finally {
            processedProducts.remove(bomItem.getComponentId());
        }
        
        return requirement;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MpsPreviewResponseDto> previewMps(List<String> quotationIds) {
        log.info("MPS 프리뷰 생성 시작: quotationIds={}", quotationIds);
        
        List<MpsPreviewResponseDto> previews = new ArrayList<>();
        
        for (String quotationId : quotationIds) {
            try {
                // Business 서비스에서 견적 데이터 조회
                BusinessQuotationDto businessQuotation = businessQuotationServicePort.getQuotationById(quotationId);
                if (businessQuotation == null || businessQuotation.getItems().isEmpty()) {
                    log.warn("MPS 프리뷰 생성 실패 - 견적 데이터를 찾을 수 없습니다: quotationId={}", quotationId);
                    continue;
                }
                
                // 각 최상위 ITEM별로 MPS 생성 (요구사항: 최상단 ITEM별로 생산)
                for (BusinessQuotationDto.BusinessQuotationItemDto item : businessQuotation.getItems()) {
                    Product product = productRepository.findById(item.getItemId()).orElse(null);
                    if (product == null || !"ITEM".equals(product.getCategory())) {
                        continue; // ITEM 타입만 MPS 대상
                    }
                    
                    MpsPreviewResponseDto preview = generateMpsPreviewForItem(
                        businessQuotation, item, product);
                    if (preview != null) {
                        previews.add(preview);
                    }
                }
                
            } catch (Exception e) {
                log.error("MPS 프리뷰 생성 중 오류 발생: quotationId={}", quotationId, e);
            }
        }
        
        return previews;
    }
    
    /**
     * 단일 아이템에 대한 MPS 프리뷰 생성
     */
    private MpsPreviewResponseDto generateMpsPreviewForItem(BusinessQuotationDto businessQuotation,
                                                           BusinessQuotationDto.BusinessQuotationItemDto item,
                                                           Product product) {
        
        String productId = item.getItemId();
        Integer requestQuantity = item.getQuantity();
        LocalDate requestDueDate = LocalDate.parse(businessQuotation.getDueDate());
        
        log.info("MPS 프리뷰 생성: productId={}, 요청수량={}, 납기={}", 
                productId, requestQuantity, requestDueDate);
        
        // BOM 조회하여 리드타임 확인
        Optional<Bom> bomOpt = bomRepository.findByProductId(productId);
        int leadTimeDays = 0;
        if (bomOpt.isPresent() && bomOpt.get().getLeadTime() != null) {
            leadTimeDays = bomOpt.get().getLeadTime().intValue();
        }
        
        // 현재 재고 확인
        Integer currentStock = getActualAvailableStock(productId);
        Integer shortageQuantity = Math.max(0, requestQuantity - currentStock);
        Integer availableFromStock = Math.min(requestQuantity, currentStock);
        
        log.info("재고 분석 - 현재재고: {}개, 재고에서공급: {}개, 생산필요: {}개", 
                currentStock, availableFromStock, shortageQuantity);
        
        // 주차별 MPS 계산
        List<MpsPreviewResponseDto.WeekDto> weeks = calculateWeeklyMps(
            shortageQuantity, requestDueDate, leadTimeDays);
        
        return MpsPreviewResponseDto.builder()
            .quotationNumber(businessQuotation.getQuotationNumber())
            .customerCompanyName(businessQuotation.getCustomerName())
            .productName(product.getProductName())
            .confirmedDueDate(requestDueDate)
            .weeks(weeks)
            .build();
    }
    
    /**
     * 주차별 MPS 계산 (요구사항에 따른 로직)
     *
     * 로직 설명:
     * 1. 요청일(오늘) + leadTime = 수요 발생일 (납품 필요일)
     * 2. 수요 발생일이 포함된 주차에 수요 표시
     * 3. 수요 발생 이전 주차들에 생산량 배분
     */
    private List<MpsPreviewResponseDto.WeekDto> calculateWeeklyMps(Integer productionQuantity,
                                                                  LocalDate dueDate,
                                                                  int leadTimeDays) {

        List<MpsPreviewResponseDto.WeekDto> weeks = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // 수요 발생일 계산: 오늘 + leadTime
        LocalDate demandDate = today.plusDays(leadTimeDays);

        log.info("MPS 계산 - 오늘: {}, 리드타임: {}일, 수요발생일: {}, 요청납기: {}",
                today, leadTimeDays, demandDate, dueDate);

        // 현재 주차부터 4주간의 주차 생성
        for (int i = 0; i < 4; i++) {
            LocalDate weekStart = today.plusWeeks(i);
            LocalDate weekEnd = weekStart.plusDays(6);
            String weekString = getWeekString(weekStart);

            int demand = 0;
            int requiredQuantity = 0;
            int production = 0;

            // 수요 발생일이 이 주차에 포함되는지 확인
            if (!demandDate.isBefore(weekStart) && !demandDate.isAfter(weekEnd)) {
                demand = productionQuantity;
                requiredQuantity = productionQuantity;
                log.info("수요 발생 주차: {} (날짜: {}) - 수요: {}개", weekString, demandDate, productionQuantity);
            }

            // 생산은 수요 발생 이전 주차에 수행
            // 현재 주차가 수요 발생 주차보다 이전이면 생산 수행
            if (weekEnd.isBefore(demandDate)) {
                production = productionQuantity;
                log.info("생산 수행 주차: {} - 생산: {}개", weekString, productionQuantity);
            }

            MpsPreviewResponseDto.WeekDto weekDto = MpsPreviewResponseDto.WeekDto.builder()
                .week(weekString)
                .demand(demand)
                .requiredQuantity(requiredQuantity)
                .productionQuantity(production)
                .mps(production > 0 ? production : null)  // MPS는 생산량이 있을 때만 표시
                .build();

            weeks.add(weekDto);
        }

        return weeks;
    }
    
        /**
     * 날짜를 주차 문자열로 변환 (월-주차 형식)
     */
    private String getWeekString(LocalDate date) {
        int year = date.getYear();
        int month = date.getMonthValue();
        
        // 해당 날짜가 몇 번째 주인지 계산
        int dayOfMonth = date.getDayOfMonth();
        int weekOfMonth = ((dayOfMonth - 1) / 7) + 1;
        
        return String.format("%d-%02d-%dW", year, month, weekOfMonth);
    }

    @Override
    @Transactional
    public void confirmQuotations(QuotationConfirmRequestDto requestDto) {
        log.info("견적 확정 처리 시작: quotationIds={}", requestDto.getQuotationIds());
        
        try {
            for (String quotationId : requestDto.getQuotationIds()) {
                // Business 서비스에서 견적 데이터 조회
                BusinessQuotationDto businessQuotation = businessQuotationServicePort.getQuotationById(quotationId);
                if (businessQuotation == null) {
                    log.warn("견적을 찾을 수 없습니다: quotationId={}", quotationId);
                    continue;
                }
                
                // 각 아이템별로 MPS/MRP 생성
                for (BusinessQuotationDto.BusinessQuotationItemDto item : businessQuotation.getItems()) {
                    Product product = productRepository.findById(item.getItemId()).orElse(null);
                    if (product == null || !"ITEM".equals(product.getCategory())) {
                        continue; // ITEM 타입만 처리
                    }
                    
                    confirmQuotationItem(quotationId, businessQuotation, item, product);
                }
                
                // PPtodo Business 서비스에 dueDate 업데이트, 견적 상태도 바꿔야함(주석 처리 - 추후 개발)
                // updateQuotationDueDate(quotationId, businessQuotation);
            }
            
            log.info("견적 확정 처리 완료");
            
        } catch (Exception e) {
            log.error("견적 확정 처리 중 오류 발생", e);
            throw new RuntimeException("견적 확정 처리 실패", e);
        }
    }
    
    /**
     * 단일 아이템에 대한 MPS/MRP 확정 처리
     *
     * 로직 설명:
     * 1. 확정 납기 = 오늘 + leadTime(생산) + 4일(배송)
     * 2. 재고에서 충당 가능한 만큼 예약
     * 3. 부족분에 대해 MPS/MRP 생성
     */
    private void confirmQuotationItem(String quotationId,
                                     BusinessQuotationDto businessQuotation,
                                     BusinessQuotationDto.BusinessQuotationItemDto item,
                                     Product product) {

        String productId = item.getItemId();
        Integer requestQuantity = item.getQuantity();
        LocalDate requestDueDate = LocalDate.parse(businessQuotation.getDueDate());
        LocalDate today = LocalDate.now();

        log.info("아이템 확정 처리: productId={}, 요청수량={}, 요청납기={}", productId, requestQuantity, requestDueDate);

        // 1. BOM 조회 및 리드타임 확인
        Optional<Bom> bomOpt = bomRepository.findByProductId(productId);
        if (bomOpt.isEmpty()) {
            log.warn("BOM을 찾을 수 없습니다: productId={}", productId);
            return;
        }

        Bom bom = bomOpt.get();
        int leadTimeDays = bom.getLeadTime() != null ? bom.getLeadTime().intValue() : 0;

        // 2. 확정 납기 계산 (오늘 + 리드타임(생산) + 배송 4일)
        LocalDate productionCompleteDate = today.plusDays(leadTimeDays);
        LocalDate confirmedDueDate = productionCompleteDate.plusDays(4);

        log.info("납기 계산 - 오늘: {}, 생산완료일: {}, 최종납기일: {}",
                today, productionCompleteDate, confirmedDueDate);

        // 3. 재고 분석
        Integer currentStock = getActualAvailableStock(productId);
        Integer shortageQuantity = Math.max(0, requestQuantity - currentStock);
        Integer availableFromStock = Math.min(requestQuantity, currentStock);

        log.info("재고 분석 - 현재재고: {}개, 재고사용: {}개, 생산필요: {}개",
                currentStock, availableFromStock, shortageQuantity);

        // 4. 완제품 재고 예약
        if (availableFromStock > 0) {
            boolean reserved = stockReservationService.reserveStock(productId, BigDecimal.valueOf(availableFromStock));
            if (!reserved) {
                log.warn("재고 예약 실패: productId={}, quantity={}", productId, availableFromStock);
            } else {
                log.info("재고 예약 완료: productId={}, quantity={}", productId, availableFromStock);
            }
        }

        // 5. MPS 생성 (생산이 필요한 경우만)
        if (shortageQuantity > 0) {
            createMpsRecords(quotationId, productId, shortageQuantity, confirmedDueDate, leadTimeDays, bom);
        }

        // 6. MRP 생성 (부족한 자재들)
        createMrpRecords(quotationId, bom, shortageQuantity, productionCompleteDate);

        // 7. MES 생성 (생산이 필요한 경우 작업 지시서 생성)
        if (shortageQuantity > 0) {
            createMesRecords(quotationId, productId, shortageQuantity, today, productionCompleteDate, bom);
        }

        log.info("아이템 확정 완료: productId={}, 재고예약={}개, 생산계획={}개, 최종납기={}",
                productId, availableFromStock, shortageQuantity, confirmedDueDate);
    }
    
    /**
     * MPS 레코드 생성
     *
     * MPS 마스터: 전체 생산 계획 기간 (시작주차 ~ 끝주차)
     * MPS Detail: 주차별 상세 생산 계획
     */
    private void createMpsRecords(String quotationId, String productId, Integer productionQuantity,
                                 LocalDate confirmedDueDate, int leadTimeDays, Bom bom) {

        log.info("MPS 생성: productId={}, 생산량={}, 납기={}", productId, productionQuantity, confirmedDueDate);

        LocalDate today = LocalDate.now();
        LocalDate productionCompleteDate = today.plusDays(leadTimeDays);

        // MPS 마스터 생성
        String mpsId = UUID.randomUUID().toString();
        Mps mps = Mps.builder()
            .id(mpsId)
            .bomId(bom.getId())
            .quotationId(quotationId)
            .startWeek(today)  // 시작일: 오늘
            .endWeek(productionCompleteDate)  // 종료일: 생산 완료일
            .build();

        mpsRepository.save(mps);
        log.info("MPS 마스터 생성: mpsId={}, 기간: {} ~ {}", mpsId, today, productionCompleteDate);

        // 생산 주차별로 MPS Detail 생성
        // 수요는 생산완료일에 발생
        LocalDate demandDate = productionCompleteDate;
        String demandWeek = getWeekString(demandDate);

        // 생산은 오늘부터 생산완료일 이전 주차에 배분
        List<LocalDate> productionWeeks = new ArrayList<>();
        LocalDate currentWeek = today;
        while (currentWeek.isBefore(demandDate)) {
            productionWeeks.add(currentWeek);
            currentWeek = currentWeek.plusWeeks(1);
        }

        // 생산량을 주차별로 분배 (간단하게 첫 주차에 모두 배치)
        if (!productionWeeks.isEmpty()) {
            LocalDate firstProductionWeek = productionWeeks.get(0);
            String firstWeekLabel = getWeekString(firstProductionWeek);

            MpsDetail productionDetail = MpsDetail.builder()
                .id(UUID.randomUUID().toString())
                .mpsId(mpsId)
                .weekLabel(firstWeekLabel)
                .demand(0)
                .requiredInventory(0)
                .productionNeeded(productionQuantity)
                .plannedProduction(productionQuantity)
                .build();

            mpsDetailRepository.save(productionDetail);
            log.info("MPS Detail 생성 (생산): week={}, 계획생산량={}", firstWeekLabel, productionQuantity);
        }

        // 수요 발생 주차의 MPS Detail
        MpsDetail demandDetail = MpsDetail.builder()
            .id(UUID.randomUUID().toString())
            .mpsId(mpsId)
            .weekLabel(demandWeek)
            .demand(productionQuantity)
            .requiredInventory(productionQuantity)
            .productionNeeded(0)
            .plannedProduction(0)
            .build();

        mpsDetailRepository.save(demandDetail);
        log.info("MPS Detail 생성 (수요): week={}, 수요량={}", demandWeek, productionQuantity);

        log.info("MPS 생성 완료: mpsId={}", mpsId);
    }
    
    /**
     * MRP 레코드 생성 (MRP Run 없이 개별 MRP만 생성)
     */
    private void createMrpRecords(String quotationId, Bom bom, Integer productionQuantity, LocalDate dueDate) {
        
        if (productionQuantity <= 0) return;
        
        log.info("MRP 생성: bomId={}, 생산량={}", bom.getId(), productionQuantity);
        
        // BOM 아이템들에 대한 MRP 생성 (MRP Run 없이)
        List<BomItem> bomItems = bomItemRepository.findByBomId(bom.getId());
        
        for (BomItem bomItem : bomItems) {
            createMrpForComponent(null, bomItem, productionQuantity, dueDate, new HashSet<>(), bom, quotationId);
        }
        
        log.info("MRP 생성 완료");
    }
    
    /**
     * 구성품목에 대한 MRP 생성 (재귀적)
     */
    private void createMrpForComponent(String unusedMrpRunId, BomItem bomItem, Integer parentQuantity, 
                                      LocalDate dueDate, Set<String> processedProducts, 
                                      Bom parentBom, String quotationId) {
        
        // 순환 참조 방지
        if (processedProducts.contains(bomItem.getComponentId())) {
            return;
        }
        processedProducts.add(bomItem.getComponentId());
        
        try {
            Product component = productRepository.findById(bomItem.getComponentId()).orElse(null);
            if (component == null) return;
            
            Integer requiredQuantity = bomItem.getCount().multiply(BigDecimal.valueOf(parentQuantity)).intValue();
            Integer currentStock = getActualAvailableStock(bomItem.getComponentId());
            Integer shortageQuantity = Math.max(0, requiredQuantity - currentStock);
            
            // 배송 시작일 계산 (공급업체 배송일 고려)
            int deliveryDays = 0;
            if (component.getSupplierCompany() != null && 
                component.getSupplierCompany().getDeliveryDays() != null) {
                deliveryDays = component.getSupplierCompany().getDeliveryDays();
            }
            
            LocalDate procurementStartDate = dueDate.minusDays(deliveryDays);
            LocalDate expectedArrivalDate = dueDate;
            
            // MRP 레코드 생성
            Mrp mrp = Mrp.builder()
                .id(UUID.randomUUID().toString())
                .bomId(parentBom.getId()) 
                .quotationId(quotationId)
                .productId(bomItem.getComponentId())
                .requiredCount(BigDecimal.valueOf(requiredQuantity))
                .procurementStart(procurementStartDate)
                .expectedArrival(expectedArrivalDate)
                .status(shortageQuantity > 0 ? "INSUFFICIENT" : "SUFFICIENT")
                .build();
            
            mrpRepository.save(mrp);
            
            log.debug("MRP 생성: productId={}, 필요량={}, 현재재고={}, 부족량={}", 
                     bomItem.getComponentId(), requiredQuantity, currentStock, shortageQuantity);
            
            // 중간제품인 경우 하위 BOM 처리
            if ("ITEM".equals(component.getCategory()) && shortageQuantity > 0) {
                Optional<Bom> subBomOpt = bomRepository.findByProductId(bomItem.getComponentId());
                if (subBomOpt.isPresent()) {
                    List<BomItem> subBomItems = bomItemRepository.findByBomId(subBomOpt.get().getId());
                    
                    for (BomItem subBomItem : subBomItems) {
                        createMrpForComponent(null, subBomItem, shortageQuantity, 
                                            expectedArrivalDate, new HashSet<>(processedProducts), 
                                            subBomOpt.get(), quotationId);
                    }
                }
            }
            
        } finally {
            processedProducts.remove(bomItem.getComponentId());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public MpsQueryResponseDto getMps(String bomId, LocalDate startDate, LocalDate endDate, int page, int size) {
        log.info("MPS 조회: bomId={}, startDate={}, endDate={}, page={}, size={}",
                bomId, startDate, endDate, page, size);

        // 1. BOM 조회 및 제품 정보 확인
        Bom bom = bomRepository.findById(bomId)
                .orElseThrow(() -> new RuntimeException("BOM을 찾을 수 없습니다: " + bomId));

        Product product = productRepository.findById(bom.getProductId()).orElse(null);
        String productName = product != null ? product.getProductName() : "알 수 없는 제품";

        // 2. 주차 범위 계산 (startDate 앞 3주차부터)
        LocalDate queryStartDate = startDate.minusWeeks(3);
        LocalDate queryEndDate = endDate;

        // 총 주차 수 계산
        long weeksBetween = java.time.temporal.ChronoUnit.WEEKS.between(queryStartDate, queryEndDate) + 1;

        // 7주차 미만이면 뒤로 확장
        if (weeksBetween < 7) {
            long weeksToAdd = 7 - weeksBetween;
            queryEndDate = queryEndDate.plusWeeks(weeksToAdd);
        }

        log.info("주차 범위 확장: {} ~ {} (총 {}주)", queryStartDate, queryEndDate,
                java.time.temporal.ChronoUnit.WEEKS.between(queryStartDate, queryEndDate) + 1);

        // 3. 해당 BOM의 모든 MPS 조회
        List<Mps> mpsList = mpsRepository.findByBomId(bomId);

        // 4. MPS Detail들을 주차별로 그룹핑
        Map<String, MpsQueryResponseDto.WeekDto> weekMap = new LinkedHashMap<>();

        // 모든 주차 초기화
        LocalDate currentWeek = queryStartDate;
        while (!currentWeek.isAfter(queryEndDate)) {
            String weekLabel = getWeekString(currentWeek);
            weekMap.put(weekLabel, MpsQueryResponseDto.WeekDto.builder()
                    .week(weekLabel)
                    .demand(0)
                    .requiredInventory(0)
                    .productionNeeded(0)
                    .plannedProduction(0)
                    .build());
            currentWeek = currentWeek.plusWeeks(1);
        }

        // MPS Detail 데이터 집계 (같은 bomId의 모든 MPS를 합산)
        for (Mps mps : mpsList) {
            List<MpsDetail> details = mpsDetailRepository.findByMpsId(mps.getId());

            for (MpsDetail detail : details) {
                String weekLabel = detail.getWeekLabel();
                if (weekMap.containsKey(weekLabel)) {
                    MpsQueryResponseDto.WeekDto weekDto = weekMap.get(weekLabel);
                    weekDto.setDemand(weekDto.getDemand() + (detail.getDemand() != null ? detail.getDemand() : 0));
                    weekDto.setRequiredInventory(weekDto.getRequiredInventory() + (detail.getRequiredInventory() != null ? detail.getRequiredInventory() : 0));
                    weekDto.setProductionNeeded(weekDto.getProductionNeeded() + (detail.getProductionNeeded() != null ? detail.getProductionNeeded() : 0));
                    weekDto.setPlannedProduction(weekDto.getPlannedProduction() + (detail.getPlannedProduction() != null ? detail.getPlannedProduction() : 0));
                }
            }
        }

        // 5. 페이징 처리
        List<MpsQueryResponseDto.WeekDto> allWeeks = new ArrayList<>(weekMap.values());
        int start = page * size;
        int end = Math.min(start + size, allWeeks.size());

        List<MpsQueryResponseDto.WeekDto> pagedWeeks = start < allWeeks.size() ?
                allWeeks.subList(start, end) : new ArrayList<>();

        log.info("MPS 조회 완료: 총 {}주차, 페이지 {}/{}", allWeeks.size(), page + 1, (allWeeks.size() + size - 1) / size);

        return MpsQueryResponseDto.builder()
                .bomId(bomId)
                .productName(productName)
                .weeks(pagedWeeks)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MrpQueryResponseDto getMrp(String bomId, String quotationId, String availableStatusCode, int page, int size) {
        log.info("MRP 조회: bomId={}, quotationId={}, statusCode={}, page={}, size={}",
                bomId, quotationId, availableStatusCode, page, size);

        // 1. 조건에 맞는 MRP 레코드 조회
        List<Mrp> mrpList;

        if (bomId != null && !bomId.isEmpty()) {
            mrpList = mrpRepository.findByBomId(bomId);
        } else if (quotationId != null && !quotationId.isEmpty()) {
            mrpList = mrpRepository.findByQuotationId(quotationId);
        } else {
            mrpList = mrpRepository.findAll();
        }

        log.info("조회된 MRP 레코드 수: {}", mrpList.size());

        // 2. productId별로 그룹핑 및 집계
        Map<String, MrpAggregation> aggregationMap = new LinkedHashMap<>();

        for (Mrp mrp : mrpList) {
            String productId = mrp.getProductId();

            MrpAggregation aggregation = aggregationMap.computeIfAbsent(productId, k -> new MrpAggregation(productId));

            // 필요량 합산
            aggregation.addRequiredQuantity(mrp.getRequiredCount());

            // 가장 빠른 조달 시작일/도착일 사용
            if (mrp.getProcurementStart() != null) {
                if (aggregation.procurementStartDate == null || mrp.getProcurementStart().isBefore(aggregation.procurementStartDate)) {
                    aggregation.procurementStartDate = mrp.getProcurementStart();
                }
            }

            if (mrp.getExpectedArrival() != null) {
                if (aggregation.expectedArrivalDate == null || mrp.getExpectedArrival().isBefore(aggregation.expectedArrivalDate)) {
                    aggregation.expectedArrivalDate = mrp.getExpectedArrival();
                }
            }
        }

        // 3. 각 원자재에 대해 재고 정보 조회 및 DTO 생성
        List<MrpQueryResponseDto.MrpItemDto> allItems = new ArrayList<>();

        for (MrpAggregation aggregation : aggregationMap.values()) {
            Product product = productRepository.findById(aggregation.productId).orElse(null);
            if (product == null) {
                log.warn("제품을 찾을 수 없습니다: {}", aggregation.productId);
                continue;
            }

            // MATERIAL만 MRP에 표시
            if (!"MATERIAL".equals(product.getCategory())) {
                continue;
            }

            // 재고 정보 조회
            ProductStock productStock = productStockRepository.findByProductId(aggregation.productId).orElse(null);

            Integer currentStock = 0;        // 물리적 재고 (availableCount)
            Integer reservedStock = 0;       // 예약 재고 (reservedCount)
            Integer actualAvailable = 0;     // 실제 사용 가능 (currentStock - reservedStock)
            Integer safetyStock = 0;

            if (productStock != null) {
                currentStock = productStock.getAvailableCount() != null ?
                        productStock.getAvailableCount().intValue() : 0;
                reservedStock = productStock.getReservedCount() != null ?
                        productStock.getReservedCount().intValue() : 0;
                actualAvailable = productStock.getActualAvailableCount() != null ?
                        productStock.getActualAvailableCount().intValue() : 0;
                safetyStock = productStock.getSafetyCount() != null ?
                        productStock.getSafetyCount().intValue() : 0;
            }

            Integer requiredQuantity = aggregation.totalRequiredQuantity.intValue();
            Integer availableStock = Math.max(0, actualAvailable - safetyStock);
            Integer shortageQuantity = Math.max(0, requiredQuantity - currentStock);

            // 상태 판단: 물리적 재고(currentStock) 기준으로 충분한지 확인
            String statusCode = currentStock >= requiredQuantity ? "SUFFICIENT" : "INSUFFICIENT";

            // 상태 필터링
            if (availableStatusCode != null && !availableStatusCode.isEmpty() &&
                    !"ALL".equalsIgnoreCase(availableStatusCode)) {
                if (!statusCode.equals(availableStatusCode)) {
                    continue;
                }
            }

            // 공급업체 정보
            String supplierCompanyName = null;
            if (product.getSupplierCompany() != null) {
                supplierCompanyName = product.getSupplierCompany().getCompanyName();
            }

            MrpQueryResponseDto.MrpItemDto itemDto = MrpQueryResponseDto.MrpItemDto.builder()
                    .itemId(aggregation.productId)
                    .itemName(product.getProductName())
                    .requiredQuantity(requiredQuantity)
                    .currentStock(currentStock)          // 물리적 재고
                    .reservedStock(reservedStock)        // 예약 재고 (추가!)
                    .actualAvailableStock(actualAvailable) // 실제 사용 가능 (추가!)
                    .safetyStock(safetyStock)
                    .availableStock(availableStock)
                    .availableStatusCode(statusCode)
                    .shortageQuantity(shortageQuantity > 0 ? shortageQuantity : null)
                    .itemType(product.getCategory())
                    .procurementStartDate(aggregation.procurementStartDate)
                    .expectedArrivalDate(aggregation.expectedArrivalDate)
                    .supplierCompanyName(supplierCompanyName)
                    .build();

            allItems.add(itemDto);
        }

        log.info("필터링 후 MRP 항목 수: {}", allItems.size());

        // 4. 페이징 처리
        int start = page * size;
        int end = Math.min(start + size, allItems.size());
        List<MrpQueryResponseDto.MrpItemDto> pagedItems = start < allItems.size() ?
                allItems.subList(start, end) : new ArrayList<>();

        MrpQueryResponseDto.PageInfo pageInfo = MrpQueryResponseDto.PageInfo.builder()
                .number(page)
                .size(size)
                .totalElements(allItems.size())
                .totalPages((allItems.size() + size - 1) / size)
                .hasNext(end < allItems.size())
                .build();

        return MrpQueryResponseDto.builder()
                .page(pageInfo)
                .content(pagedItems)
                .build();
    }

    /**
     * MRP 집계를 위한 헬퍼 클래스
     */
    private static class MrpAggregation {
        String productId;
        BigDecimal totalRequiredQuantity = BigDecimal.ZERO;
        LocalDate procurementStartDate;
        LocalDate expectedArrivalDate;

        MrpAggregation(String productId) {
            this.productId = productId;
        }

        void addRequiredQuantity(BigDecimal quantity) {
            if (quantity != null) {
                this.totalRequiredQuantity = this.totalRequiredQuantity.add(quantity);
            }
        }
    }

    /**
     * MES 레코드 생성 (작업 지시서 및 공정 로그)
     */
    private void createMesRecords(String quotationId, String productId, Integer quantity,
                                  LocalDate startDate, LocalDate endDate, Bom bom) {

        log.info("MES 생성: quotationId={}, productId={}, quantity={}", quotationId, productId, quantity);

        // 1. MES 번호 생성 (WO-YYYY-NNN 형식)
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String mesNumber = "MES-" + uuid.substring(uuid.length() - 6);

        // 2. Mes 엔티티 생성
        Mes mes = Mes.builder()
            .id(UUID.randomUUID().toString())
            .mesNumber(mesNumber)
            .quotationId(quotationId)
            .bomId(bom.getId())
            .productId(productId)
            .quantity(quantity)
            .status("PENDING")
            .startDate(startDate)
            .endDate(endDate)
            .progressRate(0)
            .build();

        mesRepository.save(mes);
        log.info("Mes 엔티티 생성 완료: mesId={}, mesNumber={}", mes.getId(), mesNumber);

        // 3. BOM의 아이템들에 대한 Routing 조회
        List<BomItem> bomItems = bomItemRepository.findByBomId(bom.getId());

        // 4. Routing 정보 수집 및 정렬
        List<Routing> routings = new ArrayList<>();
        for (BomItem bomItem : bomItems) {
            Optional<Routing> routingOpt = routingRepository.findByBomItemId(bomItem.getId());
            routingOpt.ifPresent(routings::add);
        }

        // 시퀀스 순으로 정렬
        routings.sort(Comparator.comparing(Routing::getSequence));

        // 5. MesOperationLog 생성
        if (routings.isEmpty()) {
            log.warn("BOM에 대한 Routing 정보가 없습니다: bomId={}", bom.getId());
            // Routing이 없어도 MES는 생성됨 (공정 없이)
        } else {
            for (Routing routing : routings) {
                MesOperationLog operationLog = MesOperationLog.builder()
                    .id(UUID.randomUUID().toString())
                    .mesId(mes.getId())
                    .operationId(routing.getOperationId())
                    .sequence(routing.getSequence())
                    .status("PENDING")
                    // managerId는 주석처리 (추후 개발)
                    // .managerId(null)
                    .build();

                mesOperationLogRepository.save(operationLog);

                log.info("MesOperationLog 생성: operationId={}, sequence={}",
                        routing.getOperationId(), routing.getSequence());
            }
        }

        log.info("MES 생성 완료: mesId={}, 공정 수={}", mes.getId(), routings.size());
    }

    /**
     * MES 번호 생성 (WO-YYYY-NNN 형식)
     */
    private String generateMesNumber() {
        int year = LocalDate.now().getYear();

        // 올해 생성된 MES 개수 조회하여 다음 번호 생성
        String yearPrefix = "WO-" + year + "-";
        List<Mes> existingMes = mesRepository.findAll().stream()
            .filter(mes -> mes.getMesNumber() != null && mes.getMesNumber().startsWith(yearPrefix))
            .collect(Collectors.toList());

        int nextNumber = existingMes.size() + 1;

        return String.format("WO-%d-%03d", year, nextNumber);
    }

    /**
     * 생산 요구사항 계산 결과
     */
    private static class ProductionRequirement {
        private final List<QuotationSimulateResponseDto.ShortageDto> shortages = new ArrayList<>();
        private Integer maxProductionCapacity = Integer.MAX_VALUE;
        private Integer maxDeliveryDays = 0;
        private boolean hasShortage = false;

        public List<QuotationSimulateResponseDto.ShortageDto> getShortages() { return shortages; }
        public Integer getMaxProductionCapacity() { return maxProductionCapacity; }
        public Integer getMaxDeliveryDays() { return maxDeliveryDays; }
        public boolean hasShortage() { return hasShortage; }

        public void addShortage(QuotationSimulateResponseDto.ShortageDto shortage) {
            this.shortages.add(shortage);
            this.hasShortage = true;
        }

        public void updateMaxProductionCapacity(Integer capacity) {
            this.maxProductionCapacity = Math.min(this.maxProductionCapacity, capacity);
        }

        public void updateMaxDeliveryDays(Integer days) {
            this.maxDeliveryDays = Math.max(this.maxDeliveryDays, days);
        }
    }
}
