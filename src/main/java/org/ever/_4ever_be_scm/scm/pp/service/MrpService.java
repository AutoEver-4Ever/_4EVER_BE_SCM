package org.ever._4ever_be_scm.scm.pp.service;

import org.ever._4ever_be_scm.scm.pp.dto.MrpRunConvertRequestDto;
import org.ever._4ever_be_scm.scm.pp.dto.MrpRunQueryResponseDto;

public interface MrpService {

    /**
     * MRP → MRP_RUN 계획주문 전환
     */
    void convertToMrpRun(MrpRunConvertRequestDto requestDto);

    /**
     * MRP 계획주문 목록 조회
     */
    MrpRunQueryResponseDto getMrpRunList(String status, int page, int size);

    /**
     * MRP 계획주문 승인
     */
    void approveMrpRun(String mrpRunId);

    /**
     * MRP 계획주문 거부
     */
    void rejectMrpRun(String mrpRunId);

    /**
     * MRP 계획주문 입고 처리
     */
    void receiveMrpRun(String mrpRunId);
}
