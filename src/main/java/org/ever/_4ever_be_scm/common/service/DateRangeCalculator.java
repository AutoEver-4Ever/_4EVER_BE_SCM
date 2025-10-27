package org.ever._4ever_be_scm.common.service;

import java.time.*;
import java.time.temporal.*;
import java.util.*;

public class DateRangeCalculator {

    public enum PeriodType {
        WEEK, MONTH, QUARTER, YEAR
    }

    /**
     * 기준 날짜와 기간 타입(WEEK, MONTH, QUARTER, YEAR)에 따라
     * 저번 기간과 이번 기간을 함께 반환
     */
    public static Map<String, LocalDate[]> getDateRanges(PeriodType type) {

        LocalDate baseDate =  LocalDate.now();

        return switch (type) {
            case WEEK -> getWeekRanges(baseDate);
            case MONTH -> getMonthRanges(baseDate);
            case QUARTER -> getQuarterRanges(baseDate);
            case YEAR -> getYearRanges(baseDate);
        };
    }

    /**
     * WEEK: 저번주시작요일(월) ~ 저번주 같은 요일 /
     *       이번주시작요일(월) ~ 이번주 같은 요일
     */
    private static Map<String, LocalDate[]> getWeekRanges(LocalDate baseDate) {
        Map<String, LocalDate[]> result = new LinkedHashMap<>();

        // 기준 요일
        DayOfWeek currentDayOfWeek = baseDate.getDayOfWeek();

        // 이번 주 월요일
        LocalDate thisWeekStart = baseDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        // 저번 주 월요일
        LocalDate lastWeekStart = thisWeekStart.minusWeeks(1);

        // 이번 주 동일 요일까지
        LocalDate thisWeekEnd = thisWeekStart.with(currentDayOfWeek);
        // 저번 주 동일 요일까지
        LocalDate lastWeekEnd = lastWeekStart.plusDays(
                ChronoUnit.DAYS.between(thisWeekStart, thisWeekEnd)
        );

        result.put("lastWeek", new LocalDate[]{lastWeekStart, lastWeekEnd});
        result.put("thisWeek", new LocalDate[]{thisWeekStart, thisWeekEnd});

        return result;
    }


    /**
     * MONTH: 저번달 1일부터 기준일까지, 이번달 1일부터 기준일까지
     */
    private static Map<String, LocalDate[]> getMonthRanges(LocalDate baseDate) {
        Map<String, LocalDate[]> result = new LinkedHashMap<>();

        LocalDate lastMonthStart = baseDate.minusMonths(1).withDayOfMonth(1);
        LocalDate lastMonthEnd = baseDate.minusMonths(1).withDayOfMonth(
                Math.min(baseDate.getDayOfMonth(), baseDate.minusMonths(1).lengthOfMonth()));
        LocalDate thisMonthStart = baseDate.withDayOfMonth(1);
        LocalDate thisMonthEnd = baseDate;

        result.put("lastMonth", new LocalDate[]{lastMonthStart, lastMonthEnd});
        result.put("thisMonth", new LocalDate[]{thisMonthStart, thisMonthEnd});

        return result;
    }

    /** QUARTER: 저번분기 지난만큼 ~ 이번분기 같은 만큼 */
    private static Map<String, LocalDate[]> getQuarterRanges(LocalDate baseDate) {
        Map<String, LocalDate[]> result = new LinkedHashMap<>();

        int month = baseDate.getMonthValue();
        int currentQuarter = (month - 1) / 3 + 1;
        int quarterStartMonth = (currentQuarter - 1) * 3 + 1;

        LocalDate thisQuarterStart = LocalDate.of(baseDate.getYear(), quarterStartMonth, 1);
        long daysPassedInQuarter = ChronoUnit.DAYS.between(thisQuarterStart, baseDate) + 1;

        LocalDate lastQuarterEnd = thisQuarterStart.minusDays(1);
        int lastQuarterStartMonth = ((lastQuarterEnd.getMonthValue() - 1) / 3) * 3 + 1;
        LocalDate lastQuarterStart = LocalDate.of(lastQuarterEnd.getYear(), lastQuarterStartMonth, 1);

        LocalDate lastQuarterSameProgress = lastQuarterStart.plusDays(daysPassedInQuarter - 1);
        // 지난 분기 끝일을 넘지 않도록 보정
        if (lastQuarterSameProgress.isAfter(lastQuarterEnd)) {
            lastQuarterSameProgress = lastQuarterEnd;
        }

        result.put("lastQuarter", new LocalDate[]{lastQuarterStart, lastQuarterSameProgress});
        result.put("thisQuarter", new LocalDate[]{thisQuarterStart, baseDate});

        return result;
    }


    /** YEAR: 전년도 같은 날까지 ~ 이번년도 같은 날까지 */
    private static Map<String, LocalDate[]> getYearRanges(LocalDate baseDate) {
        Map<String, LocalDate[]> result = new LinkedHashMap<>();

        LocalDate lastYearStart = baseDate.minusYears(1).withDayOfYear(1);
        LocalDate lastYearEnd = baseDate.minusYears(1);
        LocalDate thisYearStart = baseDate.withDayOfYear(1);
        LocalDate thisYearEnd = baseDate;

        result.put("lastYear", new LocalDate[]{lastYearStart, lastYearEnd});
        result.put("thisYear", new LocalDate[]{thisYearStart, thisYearEnd});

        return result;
    }
}

