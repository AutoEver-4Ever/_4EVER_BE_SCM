package org.ever._4ever_be_scm.scm.mm.service.impl;

import lombok.RequiredArgsConstructor;
import org.ever._4ever_be_scm.common.service.DateRangeCalculator;
import org.ever._4ever_be_scm.scm.mm.dto.MMStatisticsResponseDto;
import org.ever._4ever_be_scm.scm.mm.repository.ProductOrderRepository;
import org.ever._4ever_be_scm.scm.mm.repository.ProductRequestRepository;
import org.ever._4ever_be_scm.scm.mm.service.MMStatisticsService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MMStatisticsServiceImpl implements MMStatisticsService {

    private final ProductOrderRepository productOrderRepository;
    private final ProductRequestRepository productRequestRepository;

    @Override
    public MMStatisticsResponseDto getMMStatistics() {
        return MMStatisticsResponseDto.builder()
                .week(buildPeriodStatistics("WEEK"))
                .month(buildPeriodStatistics("MONTH"))
                .quarter(buildPeriodStatistics("QUARTER"))
                .year(buildPeriodStatistics("YEAR"))
                .build();
    }

    private MMStatisticsResponseDto.PeriodStatistics buildPeriodStatistics(String period) {
        Map<String, LocalDate[]> ranges = DateRangeCalculator.getDateRanges();

        String currentKey;
        String previousKey;
        switch (period) {
            case "WEEK":
                currentKey = "thisWeek";
                previousKey = "lastWeek";
                break;
            case "MONTH":
                currentKey = "thisMonth";
                previousKey = "lastMonth";
                break;
            case "QUARTER":
                currentKey = "thisQuarter";
                previousKey = "lastQuarter";
                break;
            case "YEAR":
                currentKey = "thisYear";
                previousKey = "lastYear";
                break;
            default:
                throw new IllegalArgumentException("잘못된 기간: " + period);
        }

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

        // === 현재 기간 통계 ===
        long currentPurchaseApprovalPending = countPurchaseApprovalPending(currentStart, currentEnd);
        long currentPurchaseOrderApprovalPending = countPurchaseOrderApprovalPending(currentStart, currentEnd);
        long currentPurchaseOrderAmount = calculatePurchaseOrderAmount(currentStart, currentEnd);
        long currentPurchaseRequestCount = countPurchaseRequest(currentStart, currentEnd);

        // === 이전 기간 통계 ===
        long previousPurchaseApprovalPending = countPurchaseApprovalPending(previousStart, previousEnd);
        long previousPurchaseOrderApprovalPending = countPurchaseOrderApprovalPending(previousStart, previousEnd);
        long previousPurchaseOrderAmount = calculatePurchaseOrderAmount(previousStart, previousEnd);
        long previousPurchaseRequestCount = countPurchaseRequest(previousStart, previousEnd);

        return MMStatisticsResponseDto.PeriodStatistics.builder()
                .purchaseApprovalPendingCount(MMStatisticsResponseDto.StatValue.builder()
                        .value(currentPurchaseApprovalPending)
                        .delta_rate(calculateDeltaRate(currentPurchaseApprovalPending, previousPurchaseApprovalPending))
                        .build())
                .purchaseOrderApprovalPendingCount(MMStatisticsResponseDto.StatValue.builder()
                        .value(currentPurchaseOrderApprovalPending)
                        .delta_rate(calculateDeltaRate(currentPurchaseOrderApprovalPending, previousPurchaseOrderApprovalPending))
                        .build())
                .purchaseOrderAmount(MMStatisticsResponseDto.StatValue.builder()
                        .value(currentPurchaseOrderAmount)
                        .delta_rate(calculateDeltaRate(currentPurchaseOrderAmount, previousPurchaseOrderAmount))
                        .build())
                .purchaseRequestCount(MMStatisticsResponseDto.StatValue.builder()
                        .value(currentPurchaseRequestCount)
                        .delta_rate(calculateDeltaRate(currentPurchaseRequestCount, previousPurchaseRequestCount))
                        .build())
                .build();
    }

    private long countPurchaseApprovalPending(LocalDateTime startDate, LocalDateTime endDate) {
        return productRequestRepository.countByApprovalId_ApprovalStatusAndCreatedAtBetween("PENDING", startDate, endDate);
    }

    private long countPurchaseOrderApprovalPending(LocalDateTime startDate, LocalDateTime endDate) {
        return productOrderRepository.countByApprovalId_ApprovalStatusAndCreatedAtBetween("PENDING", startDate, endDate);
    }

    private long calculatePurchaseOrderAmount(LocalDateTime startDate, LocalDateTime endDate) {
        return productOrderRepository.sumTotalPriceByOrderDateBetween(startDate, endDate)
                .orElse(BigDecimal.ZERO)
                .longValue();
    }

    private long countPurchaseRequest(LocalDateTime startDate, LocalDateTime endDate) {
        return productRequestRepository.countByCreatedAtBetween(startDate, endDate);
    }

    private BigDecimal calculateDeltaRate(long current, long previous) {
        if (previous == 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf((double) (current - previous) / previous);
    }
}
