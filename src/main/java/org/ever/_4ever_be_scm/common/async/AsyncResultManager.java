package org.ever._4ever_be_scm.common.async;

import org.ever._4ever_be_scm.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * 비동기 결과를 관리하는 인터페이스
 * @param <T> 결과 데이터 타입
 */
public interface AsyncResultManager<T> {

    /**
     * 비동기 결과 객체 등록
     * @param transactionId 트랜잭션 ID
     * @param result 비동기 결과 객체
     */
    void registerResult(String transactionId, 
            DeferredResult<ResponseEntity<ApiResponse<T>>> result);
    
    /**
     * 성공 결과 설정
     * @param transactionId 트랜잭션 ID
     * @param data 결과 데이터
     */
    void setSuccessResult(String transactionId, T data);
    
    /**
     * 성공 결과 설정 (메시지 포함)
     * @param transactionId 트랜잭션 ID
     * @param data 결과 데이터
     * @param message 성공 메시지
     * @param status HTTP 상태 코드
     */
    void setSuccessResult(String transactionId, T data, String message, HttpStatus status);
    
    /**
     * 오류 결과 설정
     * @param transactionId 트랜잭션 ID
     * @param errorMessage 오류 메시지
     * @param status HTTP 상태 코드
     */
    void setErrorResult(String transactionId, String errorMessage, HttpStatus status);

    boolean hasPendingResult(String transactionId);
}
