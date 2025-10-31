package org.ever._4ever_be_scm.scm.iv.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * IM 통계 응답 DTO
 */
@Getter
@Builder
public class ImStatisticResponseDto {
    
    /**
     * 주간 통계
     */
    private ImStatisticPeriodDto week;
    
    /**
     * 월간 통계
     */
    private ImStatisticPeriodDto month;
    
    /**
     * 분기 통계
     */
    private ImStatisticPeriodDto quarter;
    
    /**
     * 연간 통계
     */
    private ImStatisticPeriodDto year;
}
