package org.ever._4ever_be_scm.scm.iv.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * IM 통계 기간별 응답 DTO
 */
@Getter
@Builder
public class ImStatisticPeriodDto {
    
    /**
     * 총 재고 가치
     */
    private StatisticValueDto total_stock;
    
    /**
     * 입고 대기
     */
    private StatisticValueDto store_pending;
    
    /**
     * 입고 완료
     */
    private StatisticValueDto store_complete;
    
    /**
     * 출고 완료
     */
    private StatisticValueDto delivery_complete;
    
    /**
     * 출고 준비완료
     */
    private StatisticValueDto delivery_pending;
}
