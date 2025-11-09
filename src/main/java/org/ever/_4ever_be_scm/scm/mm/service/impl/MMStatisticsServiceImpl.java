package org.ever._4ever_be_scm.scm.mm.service.impl;

import lombok.RequiredArgsConstructor;
import org.ever._4ever_be_scm.common.service.DateRangeCalculator;
import org.ever._4ever_be_scm.scm.iv.entity.SupplierCompany;
import org.ever._4ever_be_scm.scm.iv.entity.SupplierUser;
import org.ever._4ever_be_scm.scm.iv.repository.SupplierCompanyRepository;
import org.ever._4ever_be_scm.scm.iv.repository.SupplierUserRepository;
import org.ever._4ever_be_scm.scm.mm.dto.MMStatisticsResponseDto;
import org.ever._4ever_be_scm.scm.mm.dto.SupplierOrderStatisticsResponseDto;
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
    private final SupplierUserRepository supplierUserRepository;
    private final SupplierCompanyRepository supplierCompanyRepository;

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
        long currentPurchaseOrderAmount = calculatePurchaseOrderAmount(currentStart, currentEnd);
        long currentPurchaseRequestCount = countPurchaseRequest(currentStart, currentEnd);

        // === 이전 기간 통계 ===
        long previousPurchaseOrderAmount = calculatePurchaseOrderAmount(previousStart, previousEnd);
        long previousPurchaseRequestCount = countPurchaseRequest(previousStart, previousEnd);

        return MMStatisticsResponseDto.PeriodStatistics.builder()
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

    private long calculatePurchaseOrderAmount(LocalDateTime startDate, LocalDateTime endDate) {
        return productOrderRepository.sumTotalPriceByOrderDateBetween(startDate, endDate)
                .orElse(BigDecimal.ZERO)
                .longValue();
    }

    private long countPurchaseRequest(LocalDateTime startDate, LocalDateTime endDate) {
        return productRequestRepository.countByCreatedAtBetween(startDate, endDate);
    }

    private BigDecimal calculateDeltaRate(long current, long previous) {
        return BigDecimal.valueOf((double) (current - previous));
    }

    @Override
    public SupplierOrderStatisticsResponseDto getSupplierOrderStatistics(String userId) {
        // 1. userId로 SupplierUser 찾기
        SupplierUser supplierUser = supplierUserRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("공급업체 사용자를 찾을 수 없습니다: " + userId));

        // 2. SupplierUser로 SupplierCompany 찾기
        SupplierCompany supplierCompany = supplierCompanyRepository.findBySupplierUser(supplierUser)
                .orElseThrow(() -> new RuntimeException("공급업체 회사를 찾을 수 없습니다: " + userId));

        String supplierCompanyName = supplierCompany.getCompanyName();

        return SupplierOrderStatisticsResponseDto.builder()
                .week(buildSupplierOrderStatistics("WEEK", supplierCompanyName))
                .month(buildSupplierOrderStatistics("MONTH", supplierCompanyName))
                .quarter(buildSupplierOrderStatistics("QUARTER", supplierCompanyName))
                .year(buildSupplierOrderStatistics("YEAR", supplierCompanyName))
                .build();
    }

    private SupplierOrderStatisticsResponseDto.PeriodStatistics buildSupplierOrderStatistics(String period, String supplierCompanyName) {
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

        LocalDateTime currentStart = currentRange[0].atStartOfDay();
        LocalDateTime currentEnd = currentRange[1].atTime(LocalTime.MAX);
        LocalDateTime previousStart = previousRange[0].atStartOfDay();
        LocalDateTime previousEnd = previousRange[1].atTime(LocalTime.MAX);

        // 현재 기간 및 이전 기간 주문 개수
        long currentOrderCount = productOrderRepository.countBySupplierCompanyNameAndCreatedAtBetween(
                supplierCompanyName, currentStart, currentEnd);
        long previousOrderCount = productOrderRepository.countBySupplierCompanyNameAndCreatedAtBetween(
                supplierCompanyName, previousStart, previousEnd);

        return SupplierOrderStatisticsResponseDto.PeriodStatistics.builder()
                .orderCount(SupplierOrderStatisticsResponseDto.StatValue.builder()
                        .value(currentOrderCount)
                        .delta_rate(calculateDeltaRate(currentOrderCount, previousOrderCount))
                        .build())
                .build();
    }
}
