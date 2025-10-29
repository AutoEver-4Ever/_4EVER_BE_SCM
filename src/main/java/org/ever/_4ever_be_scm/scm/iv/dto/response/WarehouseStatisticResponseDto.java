package org.ever._4ever_be_scm.scm.iv.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 창고 통계 응답 DTO
 */
@Getter
@Builder
public class WarehouseStatisticResponseDto {
    
    /**
     * 주간 통계
     */
    private WarehouseStatisticPeriodDto week;
    
    /**
     * 월간 통계
     */
    private WarehouseStatisticPeriodDto month;
    
    /**
     * 분기 통계
     */
    private WarehouseStatisticPeriodDto quarter;
    
    /**
     * 연간 통계
     */
    private WarehouseStatisticPeriodDto year;
}
