package org.ever._4ever_be_scm.scm.iv.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 재고 부족 통계 응답 DTO
 */
@Getter
@Builder
public class ShortageStatisticResponseDto {
    
    /**
     * 주간 통계
     */
    private ShortageStatisticPeriodDto week;
    
    /**
     * 월간 통계
     */
    private ShortageStatisticPeriodDto month;
    
    /**
     * 분기 통계
     */
    private ShortageStatisticPeriodDto quarter;
    
    /**
     * 연간 통계
     */
    private ShortageStatisticPeriodDto year;
}
