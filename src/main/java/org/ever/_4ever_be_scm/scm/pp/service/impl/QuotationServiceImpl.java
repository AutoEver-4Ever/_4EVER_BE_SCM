package org.ever._4ever_be_scm.scm.pp.service.impl;

import lombok.RequiredArgsConstructor;
import org.ever._4ever_be_scm.scm.iv.entity.Product;
import org.ever._4ever_be_scm.scm.iv.entity.ProductStock;
import org.ever._4ever_be_scm.scm.iv.entity.SupplierCompany;
import org.ever._4ever_be_scm.scm.iv.repository.ProductRepository;
import org.ever._4ever_be_scm.scm.iv.repository.ProductStockRepository;
import org.ever._4ever_be_scm.scm.pp.dto.*;
import org.ever._4ever_be_scm.scm.pp.entity.*;
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

@Service
@RequiredArgsConstructor
public class QuotationServiceImpl implements QuotationService {
    
    private final BomRepository bomRepository;
    private final BomItemRepository bomItemRepository;
    private final ProductRepository productRepository;
    private final ProductStockRepository productStockRepository;
    private final MpsRepository mpsRepository;
    private final MpsDetailRepository mpsDetailRepository;
    private final MrpRepository mrpRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<QuotationSimulateResponseDto> simulateQuotations(QuotationSimulateRequestDto requestDto, Pageable pageable) {
        List<QuotationSimulateResponseDto> simulationResults = new ArrayList<>();
        
        // 실제로는 외부 Business 서버에서 견적 정보를 가져와야 하지만, 
        // 여기서는 Mock 데이터로 시뮬레이션 진행
        for (String quotationId : requestDto.getQuotationIds()) {
            QuotationSimulateResponseDto result = simulateQuotation(quotationId);
            if (result != null) {
                simulationResults.add(result);
            }
        }
        
        // 페이지네이션 적용
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), simulationResults.size());
        List<QuotationSimulateResponseDto> paginatedResults = simulationResults.subList(start, end);
        
        return new PageImpl<>(paginatedResults, pageable, simulationResults.size());
    }

    private QuotationSimulateResponseDto simulateQuotation(String quotationId) {
        // Mock 견적 데이터 (실제로는 외부 API 호출)
        // 여기서는 예시로 하드코딩된 데이터 사용
        String productId = "sample-product-id"; // 실제로는 견적에서 가져온 productId
        Integer requestQuantity = 500; // 실제로는 견적에서 가져온 수량
        LocalDate requestDueDate = LocalDate.of(2024, 2, 15); // 실제로는 견적에서 가져온 납기
        
        // 해당 제품의 BOM 조회
        Optional<Bom> bomOpt = bomRepository.findByProductId(productId);
        if (bomOpt.isEmpty()) {
            return null; // BOM이 없으면 시뮬레이션 불가
        }
        
        Bom bom = bomOpt.get();
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            return null;
        }

        // BOM 기반으로 필요한 자재 계산
        List<BomItem> bomItems = bomItemRepository.findByBomId(bom.getId());
        List<QuotationSimulateResponseDto.ShortageDto> shortages = new ArrayList<>();
        
        Integer totalAvailableQuantity = Integer.MAX_VALUE; // 현재 생산 가능한 최대 수량
        boolean hasShortage = false;
        Integer maxDeliveryDays = 0; // 최대 배송 기간
        
        for (BomItem bomItem : bomItems) {
            Product componentProduct = productRepository.findById(bomItem.getComponentId()).orElse(null);
            if (componentProduct == null) continue;
            
            Integer requiredQuantity = bomItem.getCount().multiply(BigDecimal.valueOf(requestQuantity)).intValue();
            
            // 실제 재고 조회
            Integer currentStock = getCurrentStock(bomItem.getComponentId());
            
            if (currentStock < requiredQuantity) {
                Integer shortQuantity = requiredQuantity - currentStock;
                shortages.add(QuotationSimulateResponseDto.ShortageDto.builder()
                    .itemId(bomItem.getComponentId())
                    .itemName(componentProduct.getProductName())
                    .requiredQuantity(requiredQuantity)
                    .currentStock(currentStock)
                    .shortQuantity(shortQuantity)
                    .build());
                
                // 현재 재고로 생산 가능한 수량 계산
                Integer possibleQuantity = currentStock / bomItem.getCount().intValue();
                totalAvailableQuantity = Math.min(totalAvailableQuantity, possibleQuantity);
                
                hasShortage = true;
                
                // 공급업체 배송 기간 고려
                if (componentProduct.getSupplierCompany() != null && 
                    componentProduct.getSupplierCompany().getDeliveryDays() != null) {
                    maxDeliveryDays = Math.max(maxDeliveryDays, componentProduct.getSupplierCompany().getDeliveryDays());
                }
            }
        }
        
        if (!hasShortage) {
            totalAvailableQuantity = requestQuantity;
        }
        
        // 부족분이 있을 경우 제안 납기 계산 (BOM 리드타임 + 최대 배송 기간)
        LocalDate suggestedDueDate = requestDueDate;
        if (hasShortage) {
            Integer totalLeadTime = maxDeliveryDays;
            if (bom.getLeadTime() != null) {
                totalLeadTime += bom.getLeadTime().intValue();
            }
            suggestedDueDate = requestDueDate.plusDays(totalLeadTime);
        }
        
        return QuotationSimulateResponseDto.builder()
            .quotationId(quotationId)
            .quotationNumber("Q-2024-001") // Mock 데이터
            .customerCompanyId("1")
            .customerCompanyName("현대자동차")
            .productId(productId)
            .productName(product.getProductName())
            .requestQuantity(requestQuantity)
            .requestDueDate(requestDueDate)
            .simulation(QuotationSimulateResponseDto.SimulationDto.builder()
                .status(hasShortage ? "FAIL" : "SUCCESS")
                .availableQuantity(totalAvailableQuantity)
                .shortageQuantity(hasShortage ? (requestQuantity - totalAvailableQuantity) : 0)
                .suggestedDueDate(suggestedDueDate)
                .generatedAt(LocalDateTime.now())
                .build())
            .shortages(shortages)
            .build();
    }

    /**
     * 제품의 현재 재고량 조회
     */
    private Integer getCurrentStock(String productId) {
        List<ProductStock> productStocks = productStockRepository.findByProductId(productId);
        if (productStocks.isEmpty()) {
            return 0;
        }
        
        // 모든 창고의 가용 재고량 합계
        return productStocks.stream()
            .filter(stock -> stock.getAvailableCount() != null)
            .mapToInt(stock -> stock.getAvailableCount().intValue())
            .sum();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MpsPreviewResponseDto> previewMps(List<String> quotationIds) {
        List<MpsPreviewResponseDto> previews = new ArrayList<>();
        
        for (String quotationId : quotationIds) {
            MpsPreviewResponseDto preview = generateMpsPreview(quotationId);
            if (preview != null) {
                previews.add(preview);
            }
        }
        
        return previews;
    }

    private MpsPreviewResponseDto generateMpsPreview(String quotationId) {
        // Mock 견적 데이터 조회 (실제로는 외부 API 호출)
        String productId = "sample-product-id";
        Integer requestQuantity = 500;
        LocalDate requestDueDate = LocalDate.of(2024, 2, 28);
        
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            return null;
        }

        // 주차별 MPS 계산
        List<MpsPreviewResponseDto.WeekDto> weeks = generateWeeklyMps(requestQuantity, requestDueDate);
        
        return MpsPreviewResponseDto.builder()
            .quotationNumber("Q-2024-001") // Mock 데이터
            .customerCompanyName("현대자동차")
            .productName(product.getProductName())
            .confirmedDueDate(requestDueDate)
            .weeks(weeks)
            .build();
    }

    private List<MpsPreviewResponseDto.WeekDto> generateWeeklyMps(Integer requestQuantity, LocalDate dueDate) {
        List<MpsPreviewResponseDto.WeekDto> weeks = new ArrayList<>();
        
        // 4주간의 MPS 계산 (납기 기준으로 역산)
        LocalDate startDate = dueDate.minusWeeks(3);
        
        for (int i = 0; i < 4; i++) {
            LocalDate weekStart = startDate.plusWeeks(i);
            String weekLabel = generateWeekLabel(weekStart);
            
            // 납기 주차에 수요 배정
            Integer demand = (i == 2) ? requestQuantity : 0; // 3주차(납기)에 수요
            Integer requiredQuantity = demand;
            
            // 생산량 분산 (예: 300, 200, 0, 0)
            Integer productionQuantity = 0;
            if (i == 0) productionQuantity = 300;
            else if (i == 1) productionQuantity = 200;
            
            weeks.add(MpsPreviewResponseDto.WeekDto.builder()
                .week(weekLabel)
                .demand(demand)
                .requiredQuantity(requiredQuantity)
                .productionQuantity(productionQuantity)
                .mps(productionQuantity)
                .build());
        }
        
        return weeks;
    }

    private String generateWeekLabel(LocalDate date) {
        int month = date.getMonthValue();
        int weekOfMonth = (date.getDayOfMonth() - 1) / 7 + 1;
        return String.format("2024-%02d-%dW", month, weekOfMonth);
    }

    @Override
    @Transactional
    public void confirmQuotations(QuotationConfirmRequestDto requestDto) {
        for (String quotationId : requestDto.getQuotationIds()) {
            confirmQuotation(quotationId);
        }
    }

    private void confirmQuotation(String quotationId) {
        // 1. MPS 데이터 생성
        createMpsData(quotationId);
        
        // 2. MRP 데이터 생성
        createMrpData(quotationId);
        
        // 3. 견적 상태를 '확정'으로 변경 (실제로는 외부 API 호출)
        // updateQuotationStatus(quotationId, "CONFIRMED");
    }

    private void createMpsData(String quotationId) {
        // Mock 견적 정보
        String productId = "sample-product-id";
        
        Optional<Bom> bomOpt = bomRepository.findByProductId(productId);
        if (bomOpt.isEmpty()) {
            return;
        }
        
        Bom bom = bomOpt.get();
        
        // MPS 생성
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String mpsCode = "MPS-" + uuid.substring(uuid.length() - 6);
        
        Mps mps = Mps.builder()
            .bomId(bom.getId())
            .quotationId(quotationId)
            .mpsCode(mpsCode)
            .internalUserId("system") // 실제로는 현재 사용자 ID
            .startWeek(LocalDate.now())
            .endWeek(LocalDate.now().plusWeeks(4))
            .build();
        
        mps = mpsRepository.save(mps);
        
        // MPS Detail 생성
        List<MpsPreviewResponseDto.WeekDto> weeks = generateWeeklyMps(500, LocalDate.of(2024, 2, 28));
        for (MpsPreviewResponseDto.WeekDto week : weeks) {
            MpsDetail mpsDetail = MpsDetail.builder()
                .mpsId(mps.getId())
                .weekLabel(week.getWeek())
                .demand(week.getDemand())
                .requiredInventory(week.getRequiredQuantity())
                .productionNeeded(week.getProductionQuantity())
                .plannedProduction(week.getMps())
                .build();
            
            mpsDetailRepository.save(mpsDetail);
        }
    }

    private void createMrpData(String quotationId) {
        // Mock 견적 정보
        String productId = "sample-product-id";
        
        Optional<Bom> bomOpt = bomRepository.findByProductId(productId);
        if (bomOpt.isEmpty()) {
            return;
        }
        
        Bom bom = bomOpt.get();
        List<BomItem> bomItems = bomItemRepository.findByBomId(bom.getId());
        
        for (BomItem bomItem : bomItems) {
            // 필요 수량 계산
            BigDecimal requiredCount = bomItem.getCount().multiply(BigDecimal.valueOf(500)); // Mock 수량
            
            // 조달 일정 계산
            LocalDate procurementStart = LocalDate.now();
            LocalDate expectedArrival = procurementStart.plusDays(7); // 기본 7일
            
            // 공급업체 배송 기간 고려
            Product componentProduct = productRepository.findById(bomItem.getComponentId()).orElse(null);
            if (componentProduct != null && componentProduct.getSupplierCompany() != null &&
                componentProduct.getSupplierCompany().getDeliveryDays() != null) {
                expectedArrival = procurementStart.plusDays(componentProduct.getSupplierCompany().getDeliveryDays());
            }
            
            // 실제 재고 상태 확인
            Integer currentStock = getCurrentStock(bomItem.getComponentId());
            String status = (currentStock >= requiredCount.intValue()) ? "충족" : "부족";
            
            Mrp mrp = Mrp.builder()
                .bomId(bom.getId())
                .quotationId(quotationId)
                .productId(bomItem.getComponentId())
                .requiredCount(requiredCount)
                .procurementStart(procurementStart)
                .expectedArrival(expectedArrival)
                .status(status)
                .build();
            
            mrpRepository.save(mrp);
        }
    }
}
