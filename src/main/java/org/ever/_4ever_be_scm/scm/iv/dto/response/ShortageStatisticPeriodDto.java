package org.ever._4ever_be_scm.scm.iv.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 재고 부족 통계 기간별 응답 DTO
 */
@Getter
@Builder
public class ShortageStatisticPeriodDto {
    
    /**
     * 주의 단계 총 개수
     */
    private StatisticValueDto total_warning;
    
    /**
     * 긴급 단계 총 개수
     */
    private StatisticValueDto total_emergency;
}
