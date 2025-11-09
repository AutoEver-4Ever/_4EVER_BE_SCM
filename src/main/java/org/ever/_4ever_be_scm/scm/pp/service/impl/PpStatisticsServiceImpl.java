package org.ever._4ever_be_scm.scm.pp.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ever._4ever_be_scm.common.service.DateRangeCalculator;
import org.ever._4ever_be_scm.scm.pp.dto.PpStatisticPeriodDto;
import org.ever._4ever_be_scm.scm.pp.dto.PpStatisticsResponseDto;
import org.ever._4ever_be_scm.scm.pp.entity.Mes;
import org.ever._4ever_be_scm.scm.pp.repository.BomRepository;
import org.ever._4ever_be_scm.scm.pp.repository.MesRepository;
import org.ever._4ever_be_scm.scm.pp.service.PpStatisticsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PpStatisticsServiceImpl implements PpStatisticsService {

    private final MesRepository mesRepository;
    private final BomRepository bomRepository;

    @Override
    public PpStatisticsResponseDto getPpStatistics() {
        return PpStatisticsResponseDto.builder()
                .week(buildPpStatistic("WEEK"))
                .month(buildPpStatistic("MONTH"))
                .quarter(buildPpStatistic("QUARTER"))
                .year(buildPpStatistic("YEAR"))
                .build();
    }

    private PpStatisticPeriodDto buildPpStatistic(String period) {
        Map<String, LocalDate[]> ranges = DateRangeCalculator.getDateRanges();

        String currentKey = getCurrentKey(period);
        String previousKey = getPreviousKey(period);

        LocalDate[] currentRange = ranges.get(currentKey);
        LocalDate[] previousRange = ranges.get(previousKey);

        if (currentRange == null || previousRange == null) {
            throw new IllegalStateException("기간 계산 실패: " + period);
        }

        LocalDateTime currentStart = currentRange[0].atStartOfDay();
        LocalDateTime currentEnd = currentRange[1].atTime(LocalTime.MAX);
        LocalDateTime previousStart = previousRange[0].atStartOfDay();
        LocalDateTime previousEnd = previousRange[1].atTime(LocalTime.MAX);

        // 1. 생산중인 품목 (IN_PRODUCTION)
        long currentProductionIn = sumMesQuantityByStatus("IN_PRODUCTION", currentStart, currentEnd);
        long previousProductionIn = sumMesQuantityByStatus("IN_PRODUCTION", previousStart, previousEnd);

        // 2. 완료된 생산 (COMPLETED)
        long currentProductionCompleted = sumMesQuantityByStatus("COMPLETED", currentStart, currentEnd);
        long previousProductionCompleted = sumMesQuantityByStatus("COMPLETED", previousStart, previousEnd);

        // 3. 완제품 개수 (BOM 개수)
        // 처음부터 현재 기간 종료까지 누적
        LocalDateTime fromBeginning = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
        long currentBomCount = bomRepository.countByCreatedAtBetween(fromBeginning, currentEnd);
        long previousBomCount = bomRepository.countByCreatedAtBetween(fromBeginning, previousEnd);

        return PpStatisticPeriodDto.builder()
                .production_in(PpStatisticPeriodDto.StatValue.builder()
                        .value(currentProductionIn)
                        .delta_rate(calculateDeltaRate(currentProductionIn, previousProductionIn))
                        .build())
                .production_completed(PpStatisticPeriodDto.StatValue.builder()
                        .value(currentProductionCompleted)
                        .delta_rate(calculateDeltaRate(currentProductionCompleted, previousProductionCompleted))
                        .build())
                .bom_count(PpStatisticPeriodDto.StatValue.builder()
                        .value(currentBomCount)
                        .delta_rate(calculateDeltaRate(currentBomCount, previousBomCount))
                        .build())
                .build();
    }

    /**
     * MES 엔티티에서 특정 상태의 quantity 합계 계산
     */
    private long sumMesQuantityByStatus(String status, LocalDateTime startDate, LocalDateTime endDate) {
        List<Mes> mesList = mesRepository.findByStatusAndUpdatedAtBetween(status, startDate, endDate);
        return mesList.stream()
                .mapToLong(mes -> mes.getQuantity() != null ? mes.getQuantity() : 0)
                .sum();
    }

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
        return BigDecimal.valueOf((double) current - previous);
    }
}
