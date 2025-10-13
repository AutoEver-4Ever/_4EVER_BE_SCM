package org.ever._4ever_be_scm.scm.service;

import org.ever._4ever_be_scm.scm.dto.response.ScmResponseDto;
import org.ever._4ever_be_scm.scm.vo.ScmRequestVo;

import java.util.List;

/**
 * SCM 서비스 인터페이스
 */
public interface ScmService {

    /**
     * 재고 예약 처리
     */
    ScmResponseDto reserveStock(ScmRequestVo request);

    /**
     * SCM 트랜잭션 조회
     */
    ScmResponseDto getScmTransaction(String scmId);

    /**
     * 모든 SCM 트랜잭션 조회
     */
    List<ScmResponseDto> getAllScmTransactions();

    /**
     * 재고 해제
     */
    ScmResponseDto releaseStock(String scmId);
}
