package org.ever._4ever_be_scm.scm.iv.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 창고 통계 기간별 응답 DTO
 */
@Getter
@Builder
public class WarehouseStatisticPeriodDto {
    
    /**
     * 총 창고 개수
     */
    private StatisticValueDto total_warehouse;
    
    /**
     * 운영중인 창고 개수
     */
    private StatisticValueDto in_operation_warehouse;
}
