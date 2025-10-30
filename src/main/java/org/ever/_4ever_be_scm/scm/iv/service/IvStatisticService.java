package org.ever._4ever_be_scm.scm.iv.service;

import org.ever._4ever_be_scm.scm.iv.dto.response.ImStatisticResponseDto;
import org.ever._4ever_be_scm.scm.iv.dto.response.ShortageStatisticResponseDto;
import org.ever._4ever_be_scm.scm.iv.dto.response.WarehouseStatisticResponseDto;

/**
 * IV 통계 서비스 인터페이스
 */
public interface IvStatisticService {
    
    /**
     * 재고 부족 통계 조회
     * 
     * @return 재고 부족 통계 정보
     */
    ShortageStatisticResponseDto getShortageStatistic();
    
    /**
     * IM 통계 조회
     * 
     * @return 재고 및 입출고 현황 통계
     */
    ImStatisticResponseDto getImStatistic();
    
    /**
     * 창고 통계 조회
     * 
     * @return 창고 현황 통계
     */
    WarehouseStatisticResponseDto getWarehouseStatistic();
}
