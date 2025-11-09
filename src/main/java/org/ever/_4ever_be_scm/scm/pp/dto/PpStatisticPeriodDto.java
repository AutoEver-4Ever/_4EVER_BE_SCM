package org.ever._4ever_be_scm.scm.pp.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PpStatisticPeriodDto {

    /**
     * 생산중인 품목
     */
    private StatValue production_in;

    /**
     * 완료된 생산
     */
    private StatValue production_completed;

    /**
     * 완제품 개수
     */
    private StatValue bom_count;

    @Data
    @Builder
    public static class StatValue {
        private long value;
        private BigDecimal delta_rate;
    }
}
