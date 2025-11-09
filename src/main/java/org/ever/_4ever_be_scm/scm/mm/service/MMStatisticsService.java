package org.ever._4ever_be_scm.scm.mm.service;

import org.ever._4ever_be_scm.scm.mm.dto.MMStatisticsResponseDto;
import org.ever._4ever_be_scm.scm.mm.dto.SupplierOrderStatisticsResponseDto;

public interface MMStatisticsService {

    /**
     * MM 통계 조회
     */
    MMStatisticsResponseDto getMMStatistics();

    /**
     * 공급업체별 주문 통계 조회
     *
     * @param userId 공급업체 사용자 ID
     * @return 공급업체 주문 통계 정보
     */
    SupplierOrderStatisticsResponseDto getSupplierOrderStatistics(String userId);
}
