package org.ever._4ever_be_scm.scm.pp.service;

import org.ever._4ever_be_scm.scm.pp.dto.PpStatisticsResponseDto;

public interface PpStatisticsService {

    /**
     * PP 통계 조회
     *
     * @return PP 통계 정보
     */
    PpStatisticsResponseDto getPpStatistics();
}
