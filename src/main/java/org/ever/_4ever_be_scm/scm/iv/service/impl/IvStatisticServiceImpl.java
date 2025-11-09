package org.ever._4ever_be_scm.scm.iv.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ever._4ever_be_scm.common.service.DateRangeCalculator;
import org.ever._4ever_be_scm.scm.iv.dto.response.*;
import org.ever._4ever_be_scm.scm.iv.repository.ProductStockRepository;
import org.ever._4ever_be_scm.scm.iv.repository.WarehouseRepository;
import org.ever._4ever_be_scm.scm.iv.service.IvStatisticService;
import org.ever._4ever_be_scm.scm.mm.repository.ProductOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

/**
 * IV 통계 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class IvStatisticServiceImpl implements IvStatisticService {

    private final ProductStockRepository productStockRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductOrderRepository productOrderRepository;
    private final org.ever._4ever_be_scm.scm.iv.integration.port.SdOrderServicePort sdOrderServicePort;
    
    /**
     * 재고 부족 통계 조회
     */
    @Override
    public ShortageStatisticResponseDto getShortageStatistic() {
        return ShortageStatisticResponseDto.builder()
                .week(buildShortageStatistic("WEEK"))
                .month(buildShortageStatistic("MONTH"))
                .quarter(buildShortageStatistic("QUARTER"))
                .year(buildShortageStatistic("YEAR"))
                .build();
    }
    
    /**
     * IM 통계 조회
     */
    @Override
    public ImStatisticResponseDto getImStatistic() {
        return ImStatisticResponseDto.builder()
                .week(buildImStatistic("WEEK"))
                .month(buildImStatistic("MONTH"))
                .quarter(buildImStatistic("QUARTER"))
                .year(buildImStatistic("YEAR"))
                .build();
    }
    
    /**
     * 창고 통계 조회
     */
    @Override
    public WarehouseStatisticResponseDto getWarehouseStatistic() {
        return WarehouseStatisticResponseDto.builder()
                .week(buildWarehouseStatistic("WEEK"))
                .month(buildWarehouseStatistic("MONTH"))
                .quarter(buildWarehouseStatistic("QUARTER"))
                .year(buildWarehouseStatistic("YEAR"))
                .build();
    }
    
    // 재고 부족 통계 - 기간별
    private ShortageStatisticPeriodDto buildShortageStatistic(String period) {
        Map<String, LocalDate[]> ranges = DateRangeCalculator.getDateRanges();
        
        String currentKey = getCurrentKey(period);
        String previousKey = getPreviousKey(period);
        
        LocalDate[] currentRange = ranges.get(currentKey);
        LocalDate[] previousRange = ranges.get(previousKey);
        
        if (currentRange == null || previousRange == null) {
            throw new IllegalStateException("기간 계산 실패: " + period);
        }
        
        // LocalDate → LocalDateTime 변환
        LocalDateTime currentStart = currentRange[0].atStartOfDay();
        LocalDateTime currentEnd = currentRange[1].atTime(LocalTime.MAX);
        LocalDateTime previousStart = previousRange[0].atStartOfDay();
        LocalDateTime previousEnd = previousRange[1].atTime(LocalTime.MAX);
        
        // 현재 기간 통계
        long currentCautionCount = productStockRepository.countByStatusAndUpdatedAtBetween("CAUTION", currentStart, currentEnd);
        long currentUrgentCount = productStockRepository.countByStatusAndUpdatedAtBetween("URGENT", currentStart, currentEnd);
        
        // 이전 기간 통계
        long previousCautionCount = productStockRepository.countByStatusAndUpdatedAtBetween("CAUTION", previousStart, previousEnd);
        long previousUrgentCount = productStockRepository.countByStatusAndUpdatedAtBetween("URGENT", previousStart, previousEnd);
        
        return ShortageStatisticPeriodDto.builder()
                .total_warning(StatisticValueDto.builder()
                        .value(currentCautionCount)
                        .delta_rate(calculateDeltaRate(currentCautionCount, previousCautionCount))
                        .build())
                .total_emergency(StatisticValueDto.builder()
                        .value(currentUrgentCount)
                        .delta_rate(calculateDeltaRate(currentUrgentCount, previousUrgentCount))
                        .build())
                .build();
    }
    
    // IM 통계 - 기간별
    private ImStatisticPeriodDto buildImStatistic(String period) {
        Map<String, LocalDate[]> ranges = DateRangeCalculator.getDateRanges();

        String currentKey = getCurrentKey(period);
        String previousKey = getPreviousKey(period);

        LocalDate[] currentRange = ranges.get(currentKey);
        LocalDate[] previousRange = ranges.get(previousKey);

        if (currentRange == null || previousRange == null) {
            throw new IllegalStateException("기간 계산 실패: " + period);
        }

        // 처음부터 현재 기간 종료까지, 처음부터 이전 기간 종료까지 (총 재고 가치용)
        // LocalDateTime.MIN은 PostgreSQL 범위를 벗어나므로 합리적인 과거 날짜 사용
        LocalDateTime fromBeginning = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
        LocalDateTime currentEnd = currentRange[1].atTime(LocalTime.MAX);
        LocalDateTime previousEnd = previousRange[1].atTime(LocalTime.MAX);

        // 특정 기간 (입고완료용)
        LocalDateTime currentStart = currentRange[0].atStartOfDay();
        LocalDateTime previousStart = previousRange[0].atStartOfDay();

        // 현재 기간 통계
        // 총 재고 가치: 처음 ~ 현재까지 누적
        BigDecimal currentTotalStock = productStockRepository.sumTotalStockValueByDateBetween(fromBeginning, currentEnd)
                .orElse(BigDecimal.valueOf(0));
        // 입고 완료: 특정 기간 동안 RECEIVED로 updatedAt된 건수
        long currentReceivedCount = productOrderRepository.countByApprovalId_ApprovalStatusAndUpdatedAtBetween("DELIVERED", currentStart, currentEnd);

        // 이전 기간 통계
        // 총 재고 가치: 처음 ~ 이전까지 누적
        BigDecimal previousTotalStock = productStockRepository.sumTotalStockValueByDateBetween(fromBeginning, previousEnd)
                .orElse(BigDecimal.valueOf(0));
        // 입고 완료: 특정 기간 동안 RECEIVED로 updatedAt된 건수
        long previousReceivedCount = productOrderRepository.countByApprovalId_ApprovalStatusAndUpdatedAtBetween("DELIVERED", previousStart, previousEnd);

        // 출고 데이터 (SD 서비스에서 조회)
        long currentDeliveredCount = 0;
        long previousDeliveredCount = 0;

        try {
            // 현재 기간 출고 완료 (DELIVERED)
            org.ever._4ever_be_scm.scm.iv.integration.dto.SdOrderListResponseDto currentDeliveredResponse =
                    sdOrderServicePort.getSalesOrderList(0, Integer.MAX_VALUE, "DELIVERED",
                            currentRange[0].toString(), currentRange[1].toString());
            currentDeliveredCount = currentDeliveredResponse != null && currentDeliveredResponse.getPage() != null
                    ? currentDeliveredResponse.getPage().getTotalElements() : 0;

            // 이전 기간 출고 완료 (DELIVERED)
            org.ever._4ever_be_scm.scm.iv.integration.dto.SdOrderListResponseDto previousDeliveredResponse =
                    sdOrderServicePort.getSalesOrderList(0, Integer.MAX_VALUE, "DELIVERED",
                            previousRange[0].toString(), previousRange[1].toString());
            previousDeliveredCount = previousDeliveredResponse != null && previousDeliveredResponse.getPage() != null
                    ? previousDeliveredResponse.getPage().getTotalElements() : 0;

        } catch (Exception e) {
            log.warn("SD 서비스 호출 실패, 기본값 사용: {}", e.getMessage());
        }

        return ImStatisticPeriodDto.builder()
                .total_stock(StatisticValueDto.builder()
                        .value(currentTotalStock.longValue())
                        .delta_rate(calculateDeltaRate(currentTotalStock.longValue(), previousTotalStock.longValue()))
                        .build())
                .store_complete(StatisticValueDto.builder()
                        .value(currentReceivedCount)
                        .delta_rate(calculateDeltaRate(currentReceivedCount, previousReceivedCount))
                        .build())
                .delivery_complete(StatisticValueDto.builder()
                        .value(currentDeliveredCount)
                        .delta_rate(calculateDeltaRate(currentDeliveredCount, previousDeliveredCount))
                        .build())
                .build();
    }
    
    // 창고 통계 - 기간별
    private WarehouseStatisticPeriodDto buildWarehouseStatistic(String period) {
        Map<String, LocalDate[]> ranges = DateRangeCalculator.getDateRanges();

        String currentKey = getCurrentKey(period);
        String previousKey = getPreviousKey(period);

        LocalDate[] currentRange = ranges.get(currentKey);
        LocalDate[] previousRange = ranges.get(previousKey);

        if (currentRange == null || previousRange == null) {
            throw new IllegalStateException("기간 계산 실패: " + period);
        }

        // 처음부터 현재 기간 종료까지, 처음부터 이전 기간 종료까지
        // LocalDateTime.MIN은 PostgreSQL 범위를 벗어나므로 합리적인 과거 날짜 사용
        LocalDateTime fromBeginning = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
        LocalDateTime currentEnd = currentRange[1].atTime(LocalTime.MAX);
        LocalDateTime previousEnd = previousRange[1].atTime(LocalTime.MAX);

        // 현재 기간 통계 (처음 ~ 현재까지 누적)
        long currentTotalCount = warehouseRepository.countByCreatedAtBetween(fromBeginning, currentEnd);
        long currentActiveCount = warehouseRepository.countByStatusAndCreatedAtBetween("ACTIVE", fromBeginning, currentEnd);

        // 이전 기간 통계 (처음 ~ 이전까지 누적)
        long previousTotalCount = warehouseRepository.countByCreatedAtBetween(fromBeginning, previousEnd);
        long previousActiveCount = warehouseRepository.countByStatusAndCreatedAtBetween("ACTIVE", fromBeginning, previousEnd);

        return WarehouseStatisticPeriodDto.builder()
                .total_warehouse(StatisticValueDto.builder()
                        .value(currentTotalCount)
                        .delta_rate(calculateDeltaRate(currentTotalCount, previousTotalCount))
                        .build())
                .in_operation_warehouse(StatisticValueDto.builder()
                        .value(currentActiveCount)
                        .delta_rate(calculateDeltaRate(currentActiveCount, previousActiveCount))
                        .build())
                .build();
    }
    
    // 유틸리티 메서드들
    private String getCurrentKey(String period) {
        switch (period) {
            case "WEEK": return "thisWeek";
            case "MONTH": return "thisMonth";
            case "QUARTER": return "thisQuarter";
            case "YEAR": return "thisYear";
            default: throw new IllegalArgumentException("잘못된 기간: " + period);
        }
    }
    
    private String getPreviousKey(String period) {
        switch (period) {
            case "WEEK": return "lastWeek";
            case "MONTH": return "lastMonth";
            case "QUARTER": return "lastQuarter";
            case "YEAR": return "lastYear";
            default: throw new IllegalArgumentException("잘못된 기간: " + period);
        }
    }
    
    private BigDecimal calculateDeltaRate(long current, long previous) {
        if (previous == 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(current - previous);
    }
}
