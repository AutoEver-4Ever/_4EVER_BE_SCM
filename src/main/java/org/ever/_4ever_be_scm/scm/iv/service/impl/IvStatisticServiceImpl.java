package org.ever._4ever_be_scm.scm.iv.service.impl;

import lombok.RequiredArgsConstructor;
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
public class IvStatisticServiceImpl implements IvStatisticService {
    
    private final ProductStockRepository productStockRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductOrderRepository productOrderRepository;
    
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
        
        // LocalDate → LocalDateTime 변환
        LocalDateTime currentStart = currentRange[0].atStartOfDay();
        LocalDateTime currentEnd = currentRange[1].atTime(LocalTime.MAX);
        LocalDateTime previousStart = previousRange[0].atStartOfDay();
        LocalDateTime previousEnd = previousRange[1].atTime(LocalTime.MAX);
        
        // 현재 기간 통계
        BigDecimal currentTotalStock = productStockRepository.sumTotalStockValueByDateBetween(currentStart, currentEnd)
                .orElse(BigDecimal.valueOf(0)); // 기본값
        long currentReceivingCount = productOrderRepository.countByApprovalId_ApprovalStatusAndUpdatedAtBetween("RECEIVING", currentStart, currentEnd);
        long currentReceivedCount = productOrderRepository.countByApprovalId_ApprovalStatusAndUpdatedAtBetween("RECEIVED", currentStart, currentEnd);
        
        // 이전 기간 통계
        BigDecimal previousTotalStock = productStockRepository.sumTotalStockValueByDateBetween(previousStart, previousEnd)
                .orElse(BigDecimal.valueOf(0)); // 기본값
        long previousReceivingCount = productOrderRepository.countByApprovalId_ApprovalStatusAndUpdatedAtBetween("RECEIVING", previousStart, previousEnd);
        long previousReceivedCount = productOrderRepository.countByApprovalId_ApprovalStatusAndUpdatedAtBetween("RECEIVED", previousStart, previousEnd);
        
        // 출고 데이터 (목업)
        long[] deliveryData = getDeliveryMockData(period);
        long[] previousDeliveryData = getPreviousDeliveryMockData(period);
        
        return ImStatisticPeriodDto.builder()
                .total_stock(StatisticValueDto.builder()
                        .value(currentTotalStock.longValue())
                        .delta_rate(calculateDeltaRate(currentTotalStock.longValue(), previousTotalStock.longValue()))
                        .build())
                .store_pending(StatisticValueDto.builder()
                        .value(currentReceivingCount)
                        .delta_rate(calculateDeltaRate(currentReceivingCount, previousReceivingCount))
                        .build())
                .store_complete(StatisticValueDto.builder()
                        .value(currentReceivedCount)
                        .delta_rate(calculateDeltaRate(currentReceivedCount, previousReceivedCount))
                        .build())
                .delivery_complete(StatisticValueDto.builder()
                        .value(deliveryData[0])
                        .delta_rate(calculateDeltaRate(deliveryData[0], previousDeliveryData[0]))
                        .build())
                .delivery_pending(StatisticValueDto.builder()
                        .value(deliveryData[1])
                        .delta_rate(calculateDeltaRate(deliveryData[1], previousDeliveryData[1]))
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
        
        // LocalDate → LocalDateTime 변환
        LocalDateTime currentStart = currentRange[0].atStartOfDay();
        LocalDateTime currentEnd = currentRange[1].atTime(LocalTime.MAX);
        LocalDateTime previousStart = previousRange[0].atStartOfDay();
        LocalDateTime previousEnd = previousRange[1].atTime(LocalTime.MAX);
        
        // 현재 기간 통계
        long currentTotalCount = warehouseRepository.countByCreatedAtBetween(currentStart, currentEnd);
        long currentActiveCount = warehouseRepository.countByStatusAndCreatedAtBetween("ACTIVE", currentStart, currentEnd);
        
        // 이전 기간 통계
        long previousTotalCount = warehouseRepository.countByCreatedAtBetween(previousStart, previousEnd);
        long previousActiveCount = warehouseRepository.countByStatusAndCreatedAtBetween("ACTIVE", previousStart, previousEnd);

        // 기본값 설정 (현재 총 창고 수 사용)
        if (currentTotalCount == 0) currentTotalCount = warehouseRepository.count();
        if (currentActiveCount == 0) currentActiveCount = warehouseRepository.countByStatus("ACTIVE");
        if (previousTotalCount == 0) previousTotalCount = currentTotalCount - 1;
        if (previousActiveCount == 0) previousActiveCount = currentActiveCount - 1;
        
        return WarehouseStatisticPeriodDto.builder()
                .total_warehouse(StatisticValueDto.builder()
                        .value(String.valueOf(currentTotalCount))
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
        return BigDecimal.valueOf((double) (current - previous) / previous);
    }
    
    // 출고 목업 데이터 (외부 서버 연동 예정)
    private long[] getDeliveryMockData(String period) {
        switch (period) {
            case "WEEK": return new long[]{89L, 14L}; // [출고완료, 출고준비완료]
            case "MONTH": return new long[]{374L, 57L};
            case "QUARTER": return new long[]{1118L, 171L};
            case "YEAR": return new long[]{4572L, 682L};
            default: return new long[]{0L, 0L};
        }
    }
    
    private long[] getPreviousDeliveryMockData(String period) {
        switch (period) {
            case "WEEK": return new long[]{83L, 15L}; // 이전 기간 목업
            case "MONTH": return new long[]{350L, 58L};
            case "QUARTER": return new long[]{1065L, 175L};
            case "YEAR": return new long[]{4388L, 695L};
            default: return new long[]{0L, 0L};
        }
    }
}
