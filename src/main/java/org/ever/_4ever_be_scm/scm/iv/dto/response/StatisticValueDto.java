package org.ever._4ever_be_scm.scm.iv.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 통계 값 DTO
 */
@Getter
@Builder
public class StatisticValueDto {
    
    /**
     * 값
     */
    private Object value;
    
    /**
     * 변화율 (delta rate)
     */
    private BigDecimal deltaRate;
}
