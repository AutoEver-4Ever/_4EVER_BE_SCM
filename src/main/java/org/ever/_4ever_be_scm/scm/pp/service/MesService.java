package org.ever._4ever_be_scm.scm.pp.service;

import org.ever._4ever_be_scm.common.response.ApiResponse;
import org.ever._4ever_be_scm.scm.pp.dto.MesDetailResponseDto;
import org.ever._4ever_be_scm.scm.pp.dto.MesQueryResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;

public interface MesService {

    /**
     * MES 목록 조회
     */
    Page<MesQueryResponseDto.MesItemDto> getMesList(String quotationId, String status, Pageable pageable);

    /**
     * MES 상세 조회
     */
    MesDetailResponseDto getMesDetail(String mesId);

    /**
     * MES 시작 (비동기 - 분산 트랜잭션)
     */
    DeferredResult<ResponseEntity<ApiResponse<Void>>> startMesAsync(String mesId);

    /**
     * 공정 시작
     */
    void startOperation(String mesId, String operationId, String managerId);

    /**
     * 공정 완료
     */
    void completeOperation(String mesId, String operationId);

    /**
     * MES 완료 (비동기 - 분산 트랜잭션)
     */
    DeferredResult<ResponseEntity<ApiResponse<Void>>> completeMesAsync(String mesId);
}
