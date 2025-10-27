package org.ever._4ever_be_scm.scm.mm.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MMStatisticsResponseDto {
    private PeriodStatistics week;
    private PeriodStatistics month;
    private PeriodStatistics quarter;
    private PeriodStatistics year;
    
    @Data
    @Builder
    public static class PeriodStatistics {
        private StatValue purchaseApprovalPendingCount;
        private StatValue purchaseOrderApprovalPendingCount;
        private StatValue purchaseOrderAmount;
        private StatValue purchaseRequestCount;
    }
    
    @Data
    @Builder
    public static class StatValue {
        private long value;
        private BigDecimal delta_rate;
    }
}
