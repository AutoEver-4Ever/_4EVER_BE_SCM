package org.ever._4ever_be_scm.scm.pp.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ever._4ever_be_scm.common.response.ApiResponse;
import org.ever._4ever_be_scm.scm.iv.entity.Product;
import org.ever._4ever_be_scm.scm.iv.entity.ProductStock;
import org.ever._4ever_be_scm.scm.iv.repository.ProductRepository;
import org.ever._4ever_be_scm.scm.iv.repository.ProductStockRepository;
import org.ever._4ever_be_scm.scm.iv.service.StockReservationService;
import org.ever._4ever_be_scm.scm.mm.dto.ToggleCodeLabelDto;
import org.ever._4ever_be_scm.scm.pp.dto.*;
import org.ever._4ever_be_scm.scm.pp.entity.*;
import org.ever._4ever_be_scm.scm.pp.integration.dto.BusinessQuotationDto;
import org.ever._4ever_be_scm.scm.pp.integration.dto.BusinessQuotationListResponseDto;
import org.ever._4ever_be_scm.scm.pp.integration.port.BusinessQuotationServicePort;
import org.ever._4ever_be_scm.scm.pp.repository.*;
import org.ever._4ever_be_scm.scm.pp.service.QuotationService;
import org.ever._4ever_be_scm.infrastructure.kafka.producer.KafkaProducerService;
import org.ever.event.QuotationUpdateEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.async.DeferredResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.ever._4ever_be_scm.infrastructure.kafka.config.KafkaTopicConfig.QUOTATION_UPDATE_TOPIC;

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
    private final MrpRunRepository mrpRunRepository;
    private final MesRepository mesRepository;
    private final MesOperationLogRepository mesOperationLogRepository;
    private final RoutingRepository routingRepository;
    private final OperationRepository operationRepository;
    private final StockReservationService stockReservationService;
    private final BusinessQuotationServicePort businessQuotationServicePort;
    private final KafkaProducerService kafkaProducerService;
    private final org.ever._4ever_be_scm.common.async.GenericAsyncResultManager<Void> asyncResultManager;

    @Override
    @Transactional(readOnly = true)
    public QuotationGroupListResponseDto getQuotationList(
            String statusCode,
            String availableStatus,
            LocalDate startDate, 
            LocalDate endDate, 
            int page, 
            int size) {
        
        // Business 서비스에서 견적 목록 조회 (이미 그룹핑된 형태)
        BusinessQuotationListResponseDto businessResponse = businessQuotationServicePort
                .getQuotationList(statusCode,availableStatus, startDate, endDate, page, size);
        
        // Business 견적 데이터를 QuotationGroupDto로 변환
        List<QuotationGroupListResponseDto.QuotationGroupDto> quotationGroups = businessResponse.getContent().stream()
                .map(this::convertToQuotationGroupDto)
                .collect(Collectors.toList());
        
        // PageInfo 변환
        QuotationGroupListResponseDto.PageInfo pageInfo = QuotationGroupListResponseDto.PageInfo.builder()
                .number(businessResponse.getPage().getNumber())
                .size(businessResponse.getPage().getSize())
                .totalElements(businessResponse.getPage().getTotalElements())
                .totalPages(businessResponse.getPage().getTotalPages())
                .hasNext(businessResponse.getPage().isHasNext())
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
        LocalDate requestDueDate = LocalDate.now();  // 요청 납기일이 null이므로 오늘 날짜 사용
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

        //shortages에서 itemId가 같은 항목들을 합산
        shortages = mergeShortagesByItemId(shortages);

        // 4. 최종 가용량 계산 (요구사항: 완제품 재고만 가용량으로 계산)
        Integer totalAvailableQuantity = availableFromStock;
        Integer finalShortageQuantity = shortageQuantity; // 부족분은 그대로

        // 5. 제안 납기 계산 (오늘 날짜 + 필요 일수)
        LocalDate suggestedDueDate = LocalDate.now().plusDays(maxDeliveryDays+4);

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
            // componentType에 따라 Product 조회 방법이 다름
            Product componentProduct = null;
            String productIdForStock = bomItem.getComponentId(); // 재고 조회용 productId

            if ("ITEM".equals(bomItem.getComponentType())) {
                // ITEM의 경우 componentId는 bomId이므로 BOM에서 productId를 찾아야 함
                Optional<Bom> bomOpt = bomRepository.findById(bomItem.getComponentId());
                if (bomOpt.isPresent()) {
                    productIdForStock = bomOpt.get().getProductId();
                    componentProduct = productRepository.findById(productIdForStock).orElse(null);
                }
            } else {
                // MATERIAL의 경우 componentId가 바로 productId
                componentProduct = productRepository.findById(bomItem.getComponentId()).orElse(null);
            }

            if (componentProduct == null) {
                log.warn("구성품목을 찾을 수 없습니다: componentId={}, componentType={}",
                    bomItem.getComponentId(), bomItem.getComponentType());
                return requirement;
            }

            Integer totalRequired = bomItem.getCount().multiply(BigDecimal.valueOf(requiredQuantity)).intValue();

            if ("MATERIAL".equals(componentProduct.getCategory())) {
                // 원자재인 경우 - 직접 재고 확인
                Integer currentStock = getActualAvailableStock(productIdForStock);
                Integer shortQuantity = Math.max(0, totalRequired - currentStock);

                // 충족 여부 상관없이 원자재 정보를 항상 shortages 배열에 추가 (충족이면 shortQuantity=0)
                requirement.addShortage(QuotationSimulateResponseDto.ShortageDto.builder()
                    .itemId(bomItem.getComponentId())
                    .itemName(componentProduct.getProductName())
                    .requiredQuantity(totalRequired)
                    .currentStock(currentStock)
                    .shortQuantity(shortQuantity)
                    .build());

                log.debug("원자재 추가: itemId={}, name={}, 필요={}, 재고={}, 부족={}",
                    bomItem.getComponentId(), componentProduct.getProductName(),
                    totalRequired, currentStock, shortQuantity);

                // 이 원자재로 생산 가능한 수량 계산
                Integer maxProduction = bomItem.getCount().intValue() > 0 ?
                                       currentStock / bomItem.getCount().intValue() : Integer.MAX_VALUE;
                requirement.updateMaxProductionCapacity(maxProduction);
                
                // 공급업체 배송 기간 고려 (seconds -> days floor for LocalDate context)
                if (componentProduct.getSupplierCompany() != null && 
                    componentProduct.getSupplierCompany().getDeliveryDays() != null) {
                    int seconds = (int) componentProduct.getSupplierCompany().getDeliveryDays().getSeconds();
                    int daysFloor = seconds / 86_400;
                    requirement.updateMaxDeliveryDays(daysFloor);
                }
                
            } else if ("ITEM".equals(componentProduct.getCategory())) {
                // 중간제품인 경우 - 재귀적으로 하위 BOM 확인
                Integer componentStock = getActualAvailableStock(productIdForStock);
                Integer componentShortage = Math.max(0, totalRequired - componentStock);

                log.info("중간제품 처리: productId={}, name={}, totalRequired={}, stock={}, shortage={}",
                    productIdForStock, componentProduct.getProductName(), totalRequired, componentStock, componentShortage);

                if (componentShortage > 0) {
                    // 하위 BOM이 있는지 확인 (ITEM의 경우 componentId가 bomId)
                    Optional<Bom> subBomOpt = bomRepository.findById(bomItem.getComponentId());
                    log.info("하위 BOM 조회: bomId={}, BOM존재={}", bomItem.getComponentId(), subBomOpt.isPresent());

                    if (subBomOpt.isPresent()) {
                        List<BomItem> subBomItems = bomItemRepository.findByBomId(subBomOpt.get().getId());
                        log.info("하위 BOM 구성품 수: bomId={}, itemCount={}", subBomOpt.get().getId(), subBomItems.size());

                        for (BomItem subBomItem : subBomItems) {
                            log.info("하위 구성품 처리 시작: componentId={}", subBomItem.getComponentId());
                            ProductionRequirement subRequirement = calculateProductionRequirement(
                                subBomItem, componentShortage, new HashSet<>(processedProducts));

                            log.info("하위 구성품 처리 완료: componentId={}, shortages={}",
                                subBomItem.getComponentId(), subRequirement.getShortages().size());

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
                        log.warn("하위 BOM이 없는 중간제품: productId={}", bomItem.getComponentId());
                        // 하위 BOM이 없는 중간제품인 경우 부족분을 직접 계산
                        requirement.addShortage(QuotationSimulateResponseDto.ShortageDto.builder()
                            .itemId(bomItem.getComponentId())
                            .itemName(componentProduct.getProductName())
                            .requiredQuantity(totalRequired)
                            .currentStock(componentStock)
                            .shortQuantity(componentShortage)
                            .build());
                    }
                } else {
                    log.info("중간제품 재고 충분 - 하위 BOM 체크 스킵: productId={}, name={}",
                        bomItem.getComponentId(), componentProduct.getProductName());
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
        LocalDate requestDueDate = LocalDate.now();  // 요청 납기일이 null이므로 오늘 날짜 사용

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
        
        // 확정 납기 계산: 오늘 + 리드타임 + 배송 4일
        LocalDate confirmedDueDate = LocalDate.now().plusDays(leadTimeDays).plusDays(4);

        return MpsPreviewResponseDto.builder()
            .quotationNumber(businessQuotation.getQuotationNumber())
            .customerCompanyName(businessQuotation.getCustomerName())
            .productName(product.getProductName())
            .confirmedDueDate(confirmedDueDate)
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

        // 오늘 날짜가 포함된 주의 시작일(월요일) 찾기
        LocalDate currentWeekStart = getWeekStart(today);

        // 현재 주차부터 4주간의 주차 생성
        for (int i = 0; i < 4; i++) {
            LocalDate weekStart = currentWeekStart.plusWeeks(i);
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
     * 날짜가 포함된 주의 시작일(월요일) 반환
     */
    private LocalDate getWeekStart(LocalDate date) {
        return date.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
    }

    /**
     * 날짜를 주차 문자열로 변환 (월-주차 형식)
     */
    private String getWeekString(LocalDate date) {
        // 해당 날짜가 포함된 주의 월요일을 기준으로 계산
        LocalDate weekStart = getWeekStart(date);
        int year = weekStart.getYear();
        int month = weekStart.getMonthValue();

        // 해당 월의 첫 번째 날의 주 시작일
        LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
        LocalDate firstWeekStart = getWeekStart(firstDayOfMonth);

        // 해당 날짜가 몇 번째 주인지 계산
        long weeksBetween = java.time.temporal.ChronoUnit.WEEKS.between(firstWeekStart, weekStart);
        int weekOfMonth = (int) weeksBetween + 1;

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

                // 각 아이템별로 MPS/MRP 생성하고 최대 납기일 추적
                LocalDate maxConfirmedDueDate = null;
                for (BusinessQuotationDto.BusinessQuotationItemDto item : businessQuotation.getItems()) {
                    Product product = productRepository.findById(item.getItemId()).orElse(null);
                    if (product == null || !"ITEM".equals(product.getCategory())) {
                        continue; // ITEM 타입만 처리
                    }

                    LocalDate confirmedDueDate = confirmQuotationItem(quotationId, businessQuotation, item, product);
                    if (confirmedDueDate != null) {
                        if (maxConfirmedDueDate == null || confirmedDueDate.isAfter(maxConfirmedDueDate)) {
                            maxConfirmedDueDate = confirmedDueDate;
                        }
                    }
                }

                // Business 서비스에 확정 납기일과 견적 상태 업데이트 이벤트 발행
                if (maxConfirmedDueDate != null) {
                    String transactionId = UUID.randomUUID().toString();
                    QuotationUpdateEvent event = QuotationUpdateEvent.builder()
                        .transactionId(transactionId)
                        .quotationId(quotationId)
                        .dueDate(maxConfirmedDueDate)
                        .quotationStatus("APPROVAL")
                        .build();

                    kafkaProducerService.sendToTopic(QUOTATION_UPDATE_TOPIC, quotationId, event);
                    log.info("견적 업데이트 이벤트 발행: transactionId={}, quotationId={}, dueDate={}",
                        transactionId, quotationId, maxConfirmedDueDate);
                }
            }
            
            log.info("견적 확정 처리 완료");

        } catch (Exception e) {
            log.error("견적 확정 처리 중 오류 발생", e);
            throw new RuntimeException("견적 확정 처리 실패", e);
        }
    }

    @Override
    @Transactional
    public DeferredResult<ResponseEntity<ApiResponse<Void>>> confirmQuotationsAsync(QuotationConfirmRequestDto requestDto) {
        log.info("견적 확정 비동기 처리 시작: quotationIds={}", requestDto.getQuotationIds());

        // DeferredResult 생성 (타임아웃 30초)
        DeferredResult<ResponseEntity<ApiResponse<Void>>> deferredResult =
                new DeferredResult<>(30000L);

        // 타임아웃 처리
        deferredResult.onTimeout(() -> {
            log.warn("견적 확정 처리 타임아웃");
            deferredResult.setResult(ResponseEntity
                    .status(HttpStatus.REQUEST_TIMEOUT)
                    .body(ApiResponse.fail("처리 시간이 초과되었습니다.", HttpStatus.REQUEST_TIMEOUT)));
        });

        try {
            for (String quotationId : requestDto.getQuotationIds()) {
                // Business 서비스에서 견적 데이터 조회
                BusinessQuotationDto businessQuotation = businessQuotationServicePort.getQuotationById(quotationId);
                if (businessQuotation == null) {
                    log.warn("견적을 찾을 수 없습니다: quotationId={}", quotationId);
                    continue;
                }

                // 각 아이템별로 MPS/MRP 생성하고 최대 납기일 추적
                LocalDate maxConfirmedDueDate = null;
                for (BusinessQuotationDto.BusinessQuotationItemDto item : businessQuotation.getItems()) {
                    Product product = productRepository.findById(item.getItemId()).orElse(null);
                    if (product == null || !"ITEM".equals(product.getCategory())) {
                        continue; // ITEM 타입만 처리
                    }

                    LocalDate confirmedDueDate = confirmQuotationItem(quotationId, businessQuotation, item, product);
                    if (confirmedDueDate != null) {
                        if (maxConfirmedDueDate == null || confirmedDueDate.isAfter(maxConfirmedDueDate)) {
                            maxConfirmedDueDate = confirmedDueDate;
                        }
                    }
                }

                // Business 서비스에 확정 납기일과 견적 상태 업데이트 이벤트 발행
                if (maxConfirmedDueDate != null) {
                    String transactionId = UUID.randomUUID().toString();

                    // DeferredResult 등록
                    asyncResultManager.registerResult(transactionId, deferredResult);

                    //TODO 알람 필요 - 견적 확정 알림 (담당자, 고객)

                    QuotationUpdateEvent event = QuotationUpdateEvent.builder()
                        .transactionId(transactionId)
                        .quotationId(quotationId)
                        .dueDate(maxConfirmedDueDate)
                        .quotationStatus("APPROVAL")
                        .build();

                    kafkaProducerService.sendToTopic(QUOTATION_UPDATE_TOPIC, quotationId, event);
                    log.info("견적 업데이트 이벤트 발행: transactionId={}, quotationId={}, dueDate={}",
                        transactionId, quotationId, maxConfirmedDueDate);
                }
            }

        } catch (Exception e) {
            log.error("견적 확정 처리 중 오류 발생", e);
            deferredResult.setResult(ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("견적 확정 처리 실패: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR)));
        }

        return deferredResult;
    }

    /**
     * 단일 아이템에 대한 MPS/MRP 확정 처리
     *
     * 로직 설명:
     * 1. 확정 납기 = 오늘 + leadTime(생산) + 4일(배송)
     * 2. 재고에서 충당 가능한 만큼 예약
     * 3. 부족분에 대해 MPS/MRP 생성
     *
     * @return 확정 납기일
     */
    private LocalDate confirmQuotationItem(String quotationId,
                                     BusinessQuotationDto businessQuotation,
                                     BusinessQuotationDto.BusinessQuotationItemDto item,
                                     Product product) {

        String productId = item.getItemId();
        Integer requestQuantity = item.getQuantity();
        LocalDate requestDueDate =LocalDate.now();
        LocalDate today = LocalDate.now();

        log.info("아이템 확정 처리: productId={}, 요청수량={}, 요청납기={}", productId, requestQuantity, requestDueDate);

        // 1. BOM 조회 및 리드타임 확인
        Optional<Bom> bomOpt = bomRepository.findByProductId(productId);
        if (bomOpt.isEmpty()) {
            log.warn("BOM을 찾을 수 없습니다: productId={}", productId);
            return null;
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
            createMesRecords(quotationId, productId, shortageQuantity, null, productionCompleteDate, bom);
        }

        log.info("아이템 확정 완료: productId={}, 재고예약={}개, 생산계획={}개, 최종납기={}",
                productId, availableFromStock, shortageQuantity, confirmedDueDate);

        return confirmedDueDate;
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
        Mps mps = Mps.builder()
            .bomId(bom.getId())
            .quotationId(quotationId)
            .startWeek(today)  // 시작일: 오늘
            .endWeek(productionCompleteDate)  // 종료일: 생산 완료일
            .build();

        mpsRepository.save(mps);
        log.info("MPS 마스터 생성: 기간: {} ~ {}", today, productionCompleteDate);

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
                .mpsId(mps.getId())
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
            .mpsId(mps.getId())
            .weekLabel(demandWeek)
            .demand(productionQuantity)
            .requiredInventory(productionQuantity)
            .productionNeeded(0)
            .plannedProduction(0)
            .build();

        mpsDetailRepository.save(demandDetail);
        log.info("MPS Detail 생성 (수요): week={}, 수요량={}", demandWeek, productionQuantity);

        log.info("MPS 생성 완료: mpsId={}", mps.getId());
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

        try {
            // componentType에 따라 Product 조회 방법이 다름
            Product component = null;
            String productIdForStock = bomItem.getComponentId(); // 재고 조회용 productId

            if ("ITEM".equals(bomItem.getComponentType())) {
                // ITEM의 경우 componentId는 bomId이므로 BOM에서 productId를 찾아야 함
                Optional<Bom> bomOpt = bomRepository.findById(bomItem.getComponentId());
                if (bomOpt.isPresent()) {
                    productIdForStock = bomOpt.get().getProductId();
                    component = productRepository.findById(productIdForStock).orElse(null);
                }
            } else {
                // MATERIAL의 경우 componentId가 바로 productId
                component = productRepository.findById(bomItem.getComponentId()).orElse(null);
            }

            if (component == null) return;

            Integer requiredQuantity = bomItem.getCount().multiply(BigDecimal.valueOf(parentQuantity)).intValue();
            Integer currentStock = getActualAvailableStock(productIdForStock);
            Integer shortageQuantity = Math.max(0, requiredQuantity - currentStock);
            Integer availableFromStock = Math.min(requiredQuantity, currentStock);

            // MATERIAL(원자재)와 ITEM(중간제품) 분리 처리
            if ("MATERIAL".equals(component.getCategory())) {
                // === 원자재 처리: 재고 예약 + MRP 생성 ===

                // 원자재 재고 예약 (재고에서 충당 가능한 만큼)
                if (availableFromStock > 0) {
                    boolean reserved = stockReservationService.reserveStock(
                        productIdForStock,
                        BigDecimal.valueOf(availableFromStock)
                    );
                    if (!reserved) {
                        log.warn("원자재 재고 예약 실패: productId={}, quantity={}",
                            productIdForStock, availableFromStock);
                    } else {
                        log.info("원자재 재고 예약 완료: productId={}, quantity={}",
                            productIdForStock, availableFromStock);
                    }
                }

                // 배송 시작일 계산 (공급업체 배송일 고려)
                int deliveryDays = 0;
                if (component.getSupplierCompany() != null &&
                    component.getSupplierCompany().getDeliveryDays() != null) {
                    int seconds = (int) component.getSupplierCompany().getDeliveryDays().getSeconds();
                    deliveryDays = seconds / 86_400; // floor to day offset
                }

                LocalDate procurementStartDate = dueDate.minusDays(deliveryDays);
                LocalDate expectedArrivalDate = dueDate;

                // 기존 MRP 조회 (중복 방지)
                List<Mrp> existingMrps = mrpRepository.findByQuotationIdAndProductId(quotationId, productIdForStock);

                Mrp mrp;
                if (!existingMrps.isEmpty()) {
                    // 기존 MRP에 수량 합산
                    mrp = existingMrps.get(0);

                    BigDecimal newRequiredCount = mrp.getRequiredCount().add(BigDecimal.valueOf(requiredQuantity));
                    BigDecimal newShortageQuantity = mrp.getShortageQuantity().add(BigDecimal.valueOf(shortageQuantity));

                    mrp.setRequiredCount(newRequiredCount);
                    mrp.setShortageQuantity(newShortageQuantity);

                    // 하나라도 INSUFFICIENT면 전체 INSUFFICIENT
                    if (shortageQuantity > 0) {
                        mrp.setStatus("INSUFFICIENT");
                    }

                    // 날짜는 가장 빠른 것으로 유지
                    if (procurementStartDate != null &&
                        (mrp.getProcurementStart() == null || procurementStartDate.isBefore(mrp.getProcurementStart()))) {
                        mrp.setProcurementStart(procurementStartDate);
                    }
                    if (expectedArrivalDate != null &&
                        (mrp.getExpectedArrival() == null || expectedArrivalDate.isBefore(mrp.getExpectedArrival()))) {
                        mrp.setExpectedArrival(expectedArrivalDate);
                    }

                    mrpRepository.save(mrp);

                    log.info("MRP 수량 합산: productId={}, name={}, category={}, 기존필요량={}, 추가필요량={}, 합계필요량={}, 기존부족량={}, 추가부족량={}, 합계부족량={}",
                             productIdForStock, component.getProductName(), component.getCategory(),
                             mrp.getRequiredCount().subtract(BigDecimal.valueOf(requiredQuantity)).intValue(),
                             requiredQuantity,
                             newRequiredCount.intValue(),
                             mrp.getShortageQuantity().subtract(BigDecimal.valueOf(shortageQuantity)).intValue(),
                             shortageQuantity,
                             newShortageQuantity.intValue());

                } else {
                    // 새 MRP 생성
                    mrp = Mrp.builder()
                        .bomId(parentBom.getId())
                        .quotationId(quotationId)
                        .productId(productIdForStock)
                        .requiredCount(BigDecimal.valueOf(requiredQuantity))
                        .shortageQuantity(BigDecimal.valueOf(shortageQuantity))
                        .consumedCount(BigDecimal.ZERO)
                        .procurementStart(procurementStartDate)
                        .expectedArrival(expectedArrivalDate)
                        .status(shortageQuantity > 0 ? "INSUFFICIENT" : "SUFFICIENT")
                        .build();

                    mrpRepository.save(mrp);

                    log.info("MRP 생성: productId={}, name={}, category={}, 필요량={}, 현재재고={}, 재고예약={}, 부족량={}, 소비량=0",
                             productIdForStock, component.getProductName(), component.getCategory(),
                             requiredQuantity, currentStock, availableFromStock, shortageQuantity);
                }

            } else if ("ITEM".equals(component.getCategory()) && shortageQuantity > 0) {
                // === 중간제품 처리: 순환 참조 체크 + 하위 BOM만 전개 ===

                // 중간제품 순환 참조 방지
                if (processedProducts.contains(productIdForStock)) {
                    log.warn("중간제품 순환 참조 감지 및 스킵: productId={}", productIdForStock);
                    return;
                }
                processedProducts.add(productIdForStock);

                log.info("중간제품 하위 BOM 처리 시작: bomId={}, shortage={}", bomItem.getComponentId(), shortageQuantity);
                Optional<Bom> subBomOpt = bomRepository.findById(bomItem.getComponentId());
                log.info("하위 BOM 조회 결과: bomId={}, BOM존재={}", bomItem.getComponentId(), subBomOpt.isPresent());

                if (subBomOpt.isPresent()) {
                    List<BomItem> subBomItems = bomItemRepository.findByBomId(subBomOpt.get().getId());
                    log.info("하위 BOM 구성품 수: bomId={}, itemCount={}", subBomOpt.get().getId(), subBomItems.size());

                    for (BomItem subBomItem : subBomItems) {
                        log.info("하위 구성품 MRP 생성 시작: componentId={}", subBomItem.getComponentId());
                        createMrpForComponent(null, subBomItem, shortageQuantity,
                                            dueDate, processedProducts,
                                            subBomOpt.get(), quotationId);
                    }
                } else {
                    log.warn("하위 BOM이 없는 중간제품: productId={}", bomItem.getComponentId());
                }
            } else if ("ITEM".equals(component.getCategory()) && shortageQuantity == 0) {
                log.info("중간제품 재고 충분 - 하위 MRP 생성 스킵: productId={}, name={}",
                    bomItem.getComponentId(), component.getProductName());
            }

        } catch (Exception e) {
            log.error("MRP 생성 중 오류 발생: componentId={}", bomItem.getComponentId(), e);
            throw e;
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

        // 2. 주차 범위 계산 (startDate가 포함된 주차 앞 3주차부터)
        // startDate와 endDate가 포함된 주의 시작일(월요일) 찾기
        LocalDate startWeekStart = getWeekStart(startDate);
        LocalDate endWeekStart = getWeekStart(endDate);

        // startDate가 포함된 주차 앞 3주차부터 시작
        LocalDate queryStartDate = startWeekStart.minusWeeks(3);
        LocalDate queryEndDate = endWeekStart;

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

        // 모든 주차 초기화 (주 시작일 기준)
        LocalDate currentWeekStart = queryStartDate;
        while (!currentWeekStart.isAfter(queryEndDate)) {
            String weekLabel = getWeekString(currentWeekStart);
            weekMap.put(weekLabel, MpsQueryResponseDto.WeekDto.builder()
                    .week(weekLabel)
                    .demand(0)
                    .requiredInventory(0)
                    .productionNeeded(0)
                    .plannedProduction(0)
                    .build());
            currentWeekStart = currentWeekStart.plusWeeks(1);
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

        // 6. PageInfo 생성
        MpsQueryResponseDto.PageInfo pageInfo = MpsQueryResponseDto.PageInfo.builder()
                .number(page)
                .size(size)
                .totalElements(allWeeks.size())
                .totalPages((allWeeks.size() + size - 1) / size)
                .hasNext(end < allWeeks.size())
                .build();

        return MpsQueryResponseDto.builder()
                .bomId(bomId)
                .productName(productName)
                .content(pagedWeeks)
                .page(pageInfo)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MrpQueryResponseDto.MrpItemDto> getMrp(String bomId, String quotationId, String availableStatusCode, int page, int size) {
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

        // 2. quotationId + productId 조합으로 그룹핑 및 집계
        Map<String, MrpAggregation> aggregationMap = new LinkedHashMap<>();

        for (Mrp mrp : mrpList) {
            String quotationIdKey = mrp.getQuotationId();
            String productId = mrp.getProductId();

            // quotationId + productId 조합 키
            String compositeKey = quotationIdKey + ":" + productId;

            MrpAggregation aggregation = aggregationMap.computeIfAbsent(
                compositeKey,
                k -> new MrpAggregation(quotationIdKey, productId)
            );

            // 필요량, 부족량, 소비량 합산
            aggregation.addRequiredQuantity(mrp.getRequiredCount());
            aggregation.addShortageQuantity(mrp.getShortageQuantity());
            aggregation.addConsumedCount(mrp.getConsumedCount());

            // 하나라도 INSUFFICIENT면 전체 INSUFFICIENT
            if ("INSUFFICIENT".equals(mrp.getStatus())) {
                aggregation.status = "INSUFFICIENT";
            }

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

        // 3. 각 원자재에 대해 DTO 생성 (MRP 테이블 데이터만 사용)
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

            Integer requiredQuantity = aggregation.totalRequiredQuantity.intValue();
            Integer shortageQuantity = aggregation.totalShortageQuantity.intValue();
            Integer consumptionQuantity = aggregation.totalConsumedCount.intValue();
            String statusCode = aggregation.status;

            // availableStock 계산: INSUFFICIENT면 requiredQuantity - shortageQuantity, SUFFICIENT면 shortageQuantity
            Integer availableStock;
            if ("INSUFFICIENT".equals(statusCode)) {
                availableStock = requiredQuantity - shortageQuantity;
            } else {
                availableStock = shortageQuantity;
            }

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

            // convertStatus 계산
            String convertStatus = "NOT_CONVERTED";  // 기본값
            List<Mrp> mrps = mrpRepository.findByQuotationIdAndProductId(aggregation.quotationId, aggregation.productId);
            if (!mrps.isEmpty()) {
                Mrp mrp = mrps.get(0);  // quotationId + productId 조합은 유일
                List<MrpRun> mrpRuns = mrpRunRepository.findByMrpId(mrp.getId());

                if ("SUFFICIENT".equals(statusCode) && mrpRuns.isEmpty()) {
                    // SUFFICIENT인데 MRP Run이 없으면
                    convertStatus = "NOT_REQUIRED";
                } else if (!mrpRuns.isEmpty()) {
                    // MRP Run이 있으면
                    convertStatus = "CONVERTED";
                }
                // 그 외의 경우는 기본값 "NOT_CONVERTED" 유지
            }

            MrpQueryResponseDto.MrpItemDto itemDto = MrpQueryResponseDto.MrpItemDto.builder()
                    .quotationId(aggregation.quotationId)
                    .itemId(aggregation.productId)
                    .itemName(product.getProductName())
                    .requiredQuantity(requiredQuantity)
                    .availableStock(availableStock)
                    .availableStatusCode(statusCode)
                    .shortageQuantity(shortageQuantity)
                    .consumptionQuantity(consumptionQuantity)
                    .itemType(product.getCategory())
                    .procurementStartDate(aggregation.procurementStartDate)
                    .expectedArrivalDate(aggregation.expectedArrivalDate)
                    .supplierCompanyName(supplierCompanyName)
                    .convertStatus(convertStatus)
                    .build();

            allItems.add(itemDto);
        }

        log.info("필터링 후 MRP 항목 수: {}", allItems.size());

        // 4. 페이징 처리
        int start = page * size;
        int end = Math.min(start + size, allItems.size());
        List<MrpQueryResponseDto.MrpItemDto> pagedItems = start < allItems.size() ?
                allItems.subList(start, end) : new ArrayList<>();

        return new org.springframework.data.domain.PageImpl<>(
                pagedItems,
                org.springframework.data.domain.PageRequest.of(page, size),
                allItems.size()
        );
    }

    /**
     * MRP 집계를 위한 헬퍼 클래스 (quotationId + productId 조합)
     */
    private static class MrpAggregation {
        String quotationId;
        String productId;
        BigDecimal totalRequiredQuantity = BigDecimal.ZERO;
        BigDecimal totalShortageQuantity = BigDecimal.ZERO;
        BigDecimal totalConsumedCount = BigDecimal.ZERO;
        String status = "SUFFICIENT";  // 기본값 SUFFICIENT, 하나라도 INSUFFICIENT면 변경
        LocalDate procurementStartDate;
        LocalDate expectedArrivalDate;

        MrpAggregation(String quotationId, String productId) {
            this.quotationId = quotationId;
            this.productId = productId;
        }

        void addRequiredQuantity(BigDecimal quantity) {
            if (quantity != null) {
                this.totalRequiredQuantity = this.totalRequiredQuantity.add(quantity);
            }
        }

        void addShortageQuantity(BigDecimal quantity) {
            if (quantity != null) {
                this.totalShortageQuantity = this.totalShortageQuantity.add(quantity);
            }
        }

        void addConsumedCount(BigDecimal quantity) {
            if (quantity != null) {
                this.totalConsumedCount = this.totalConsumedCount.add(quantity);
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

        // 3. BOM 상세조회와 동일한 방식으로 Routing 수집 (순서 보장)
        List<BomItem> bomItems = bomItemRepository.findByBomId(bom.getId());
        List<Routing> routings = collectRoutingsInOrderForMes(bomItems);

        // 4. MesOperationLog 생성 (sequence를 1부터 재부여)
        if (routings.isEmpty()) {
            log.warn("BOM에 대한 Routing 정보가 없습니다: bomId={}", bom.getId());
            // Routing이 없어도 MES는 생성됨 (공정 없이)
        } else {
            int sequenceNum = 1;  // 수집된 순서대로 1부터 부여
            for (Routing routing : routings) {
                MesOperationLog operationLog = MesOperationLog.builder()
                    .id(UUID.randomUUID().toString())
                    .mesId(mes.getId())
                    .operationId(routing.getOperationId())
                    .sequence(sequenceNum)  // 1부터 순차적으로 부여
                    .status("PENDING")
                    // managerId는 주석처리 (추후 개발)
                    // .managerId(null)
                    .build();

                mesOperationLogRepository.save(operationLog);

                log.info("MesOperationLog 생성: operationId={}, sequence={}",
                        routing.getOperationId(), sequenceNum);

                sequenceNum++;
            }
        }

        log.info("MES 생성 완료: mesId={}, 공정 수={}", mes.getId(), routings.size());
    }

    /**
     * BOM 상세조회와 동일한 방식으로 Routing 수집 (MES 생성용)
     * BomItem을 routing sequence 순으로 처리하여 올바른 순서로 routing 수집
     * ITEM의 경우 하위 BOM routing을 먼저 추가한 후, ITEM 자체 routing을 추가
     */
    private List<Routing> collectRoutingsInOrderForMes(List<BomItem> bomItems) {
        List<Routing> result = new ArrayList<>();

        // 1. BomItem을 routing sequence로 정렬하기 위해 <BomItem, Routing> 쌍으로 수집
        List<BomItemRoutingPair> itemsWithRouting = new ArrayList<>();
        for (BomItem bomItem : bomItems) {
            Optional<Routing> routingOpt = routingRepository.findByBomItemId(bomItem.getId());
            if (routingOpt.isPresent()) {
                itemsWithRouting.add(new BomItemRoutingPair(bomItem, routingOpt.get()));
            }
        }

        // 2. routing sequence 순으로 정렬
        itemsWithRouting.sort(Comparator.comparingInt(item -> item.routing.getSequence()));

        // 3. 정렬된 순서대로 처리
        for (BomItemRoutingPair item : itemsWithRouting) {
            BomItem bomItem = item.bomItem;
            Routing routing = item.routing;

            if ("ITEM".equals(bomItem.getComponentType())) {
                // ITEM의 경우: 하위 BOM의 routing을 먼저 수집
                String childBomId = bomItem.getComponentId(); // ITEM의 componentId는 BOM ID
                List<BomItem> childBomItems = bomItemRepository.findByBomId(childBomId);
                List<Routing> childRoutings = collectRoutingsInOrderForMes(childBomItems);
                result.addAll(childRoutings);

                log.debug("중첩된 ITEM 처리: bomItemId={}, childBomId={}, 하위 routing {}개 수집",
                        bomItem.getId(), childBomId, childRoutings.size());

                // 그 다음에 ITEM 자체의 routing 추가 (조립 공정)
                result.add(routing);
                log.debug("ITEM 조립 Routing 추가: bomItemId={}, operationId={}, sequence={}",
                        bomItem.getId(), routing.getOperationId(), routing.getSequence());

            } else {
                // MATERIAL의 경우: routing 그대로 추가
                result.add(routing);
                log.debug("MATERIAL Routing 추가: bomItemId={}, operationId={}, sequence={}",
                        bomItem.getId(), routing.getOperationId(), routing.getSequence());
            }
        }

        return result;
    }

    /**
     * BomItem과 Routing을 함께 저장하는 헬퍼 클래스
     */
    private static class BomItemRoutingPair {
        BomItem bomItem;
        Routing routing;

        BomItemRoutingPair(BomItem bomItem, Routing routing) {
            this.bomItem = bomItem;
            this.routing = routing;
        }
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

    /**
     * itemId가 같은 shortage들을 합산
     * - requiredQuantity: 합산
     * - currentStock: 첫 번째 값 유지 (모두 같은 값)
     * - shortQuantity: 합산된 requiredQuantity - currentStock
     */
    private List<QuotationSimulateResponseDto.ShortageDto> mergeShortagesByItemId(
            List<QuotationSimulateResponseDto.ShortageDto> shortages) {

        Map<String, QuotationSimulateResponseDto.ShortageDto> mergedMap = new LinkedHashMap<>();

        for (QuotationSimulateResponseDto.ShortageDto shortage : shortages) {
            String itemId = shortage.getItemId();

            if (mergedMap.containsKey(itemId)) {
                // 이미 존재하면 requiredQuantity만 합산
                QuotationSimulateResponseDto.ShortageDto existing = mergedMap.get(itemId);
                int newRequiredQuantity = existing.getRequiredQuantity() + shortage.getRequiredQuantity();
                int newShortQuantity = Math.max(0, newRequiredQuantity - existing.getCurrentStock());

                existing.setRequiredQuantity(newRequiredQuantity);
                existing.setShortQuantity(newShortQuantity);

                log.debug("Shortage 합산: itemId={}, 필요수량={}+{}={}, 현재재고={}, 부족수량={}",
                        itemId, existing.getRequiredQuantity() - shortage.getRequiredQuantity(),
                        shortage.getRequiredQuantity(), newRequiredQuantity,
                        existing.getCurrentStock(), newShortQuantity);
            } else {
                // 새로운 항목이면 추가
                mergedMap.put(itemId, QuotationSimulateResponseDto.ShortageDto.builder()
                        .itemId(shortage.getItemId())
                        .itemName(shortage.getItemName())
                        .requiredQuantity(shortage.getRequiredQuantity())
                        .currentStock(shortage.getCurrentStock())
                        .shortQuantity(shortage.getShortQuantity())
                        .build());
            }
        }

        return new ArrayList<>(mergedMap.values());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ToggleCodeLabelDto> getBomList() {
        log.info("BOM 목록 조회 시작");

        // 모든 BOM 조회
        List<Bom> bomList = bomRepository.findAll();

        // ToggleCodeLabelDto로 변환
        List<ToggleCodeLabelDto> result = new ArrayList<>();

        for (Bom bom : bomList) {
            // BOM의 productId로 Product 조회
            Product product = productRepository.findById(bom.getProductId()).orElse(null);

            if (product != null) {
                ToggleCodeLabelDto dto =
                    new ToggleCodeLabelDto(
                        product.getProductName(),  // value: productName
                        bom.getId()                // key: bomId
                    );
                result.add(dto);
            } else {
                log.warn("BOM의 Product를 찾을 수 없습니다: bomId={}, productId={}", bom.getId(), bom.getProductId());
            }
        }

        log.info("BOM 목록 조회 완료: {}개", result.size());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ToggleCodeLabelDto> getMrpQuotationList() {
        log.info("MRP 견적 목록 조회 시작");

        // MRP 테이블에서 중복 제거한 quotationId 조회
        List<String> quotationIds = mrpRepository.findDistinctQuotationIds();

        log.info("조회된 견적 ID 수: {}", quotationIds.size());

        // ToggleCodeLabelDto로 변환
        List<ToggleCodeLabelDto> result = new ArrayList<>();

        for (String quotationId : quotationIds) {
            String quotationNumber = null;

            try {
                // Business 서비스에서 견적 번호 조회
                BusinessQuotationDto quotation = businessQuotationServicePort.getQuotationById(quotationId);
                quotationNumber = quotation != null ? quotation.getQuotationNumber() : quotationId;
            } catch (Exception e) {
                log.warn("견적 정보 조회 실패: quotationId={}", quotationId);
                quotationNumber = quotationId;
            }

            ToggleCodeLabelDto dto = new ToggleCodeLabelDto(
                quotationNumber,  // value: quotationNumber
                quotationId       // key: quotationId
            );
            result.add(dto);
        }

        log.info("MRP 견적 목록 조회 완료: {}개", result.size());
        return result;
    }
}
