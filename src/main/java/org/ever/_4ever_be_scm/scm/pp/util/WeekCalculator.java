package org.ever._4ever_be_scm.scm.pp.util;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class WeekCalculator {
    
    private static final WeekFields WEEK_FIELDS = WeekFields.of(Locale.getDefault());
    
    /**
     * 날짜를 "YYYY-MM-Ww" 형식으로 변환
     * 예: 2025-11-03 -> "2025-11-1W"
     */
    public static String getWeekString(LocalDate date) {
        int year = date.getYear();
        int month = date.getMonthValue();
        int weekOfMonth = date.get(WEEK_FIELDS.weekOfMonth());
        
        return String.format("%d-%02d-%dW", year, month, weekOfMonth);
    }
    
    /**
     * "YYYY-MM-Ww" 형식 문자열을 날짜로 변환 (해당 주의 월요일)
     */
    public static LocalDate parseWeekString(String weekString) {
        // "2025-11-1W" -> year=2025, month=11, week=1
        String[] parts = weekString.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        int week = Integer.parseInt(parts[2].replace("W", ""));
        
        // 해당 월의 첫 번째 날
        LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
        
        // 첫 번째 주의 월요일 찾기
        LocalDate firstMonday = firstDayOfMonth.with(WEEK_FIELDS.dayOfWeek(), 1);
        if (firstMonday.isBefore(firstDayOfMonth)) {
            firstMonday = firstMonday.plusWeeks(1);
        }
        
        // week-1만큼 주를 더해서 해당 주의 월요일 반환
        return firstMonday.plusWeeks(week - 1);
    }
    
    /**
     * 두 날짜 사이의 주차 리스트 생성
     */
    public static java.util.List<String> getWeeksBetween(LocalDate startDate, LocalDate endDate) {
        java.util.List<String> weeks = new java.util.ArrayList<>();
        LocalDate current = startDate;
        
        while (!current.isAfter(endDate)) {
            weeks.add(getWeekString(current));
            current = current.plusWeeks(1);
        }
        
        return weeks;
    }
    
    /**
     * 날짜가 포함된 주의 시작일(월요일) 반환
     */
    public static LocalDate getWeekStartDate(LocalDate date) {
        return date.with(WEEK_FIELDS.dayOfWeek(), 1);
    }
    
    /**
     * 날짜가 포함된 주의 종료일(일요일) 반환
     */
    public static LocalDate getWeekEndDate(LocalDate date) {
        return date.with(WEEK_FIELDS.dayOfWeek(), 7);
    }
    
    /**
     * 현재 날짜부터 몇 주 후까지의 주차 리스트 생성
     */
    public static java.util.List<String> getNextWeeks(int numberOfWeeks) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusWeeks(numberOfWeeks - 1);
        return getWeeksBetween(getWeekStartDate(today), endDate);
    }
}
