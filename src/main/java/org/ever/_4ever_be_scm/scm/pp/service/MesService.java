package org.ever._4ever_be_scm.scm.pp.service;

import org.ever._4ever_be_scm.scm.pp.dto.MesDetailResponseDto;
import org.ever._4ever_be_scm.scm.pp.dto.MesQueryResponseDto;

public interface MesService {

    /**
     * MES 목록 조회
     */
    MesQueryResponseDto getMesList(String quotationId, String status, int page, int size);

    /**
     * MES 상세 조회
     */
    MesDetailResponseDto getMesDetail(String mesId);

    /**
     * MES 시작
     */
    void startMes(String mesId);

    /**
     * 공정 시작
     */
    void startOperation(String mesId, String operationId, String managerId);

    /**
     * 공정 완료
     */
    void completeOperation(String mesId, String operationId);

    /**
     * MES 완료
     */
    void completeMes(String mesId);
}
