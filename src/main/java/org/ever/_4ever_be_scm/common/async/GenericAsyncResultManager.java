package org.ever._4ever_be_scm.common.async;

import lombok.extern.slf4j.Slf4j;
import org.ever._4ever_be_scm.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 비동기 결과 관리자 구현체
 * @param <T> 결과 데이터 타입
 */
@Slf4j
@Component
public class GenericAsyncResultManager<T> implements AsyncResultManager<T> {

    // 트랜잭션 ID를 키로 하는 DeferredResult 저장소
    private final Map<String, DeferredResult<ResponseEntity<ApiResponse<T>>>> pendingResults =
            new ConcurrentHashMap<>();
    
    @Override
    public void registerResult(String transactionId, 
            DeferredResult<ResponseEntity<ApiResponse<T>>> result) {
        log.info("트랜잭션 {} 에 대한 DeferredResult 등록", transactionId);
        pendingResults.put(transactionId, result);
        
        // DeferredResult가 완료되면 맵에서 제거
        result.onCompletion(() -> {
            log.info("트랜잭션 {} 완료, DeferredResult 맵에서 제거", transactionId);
            pendingResults.remove(transactionId);
        });
    }
    
    @Override
    public void setSuccessResult(String transactionId, T data) {
        setSuccessResult(transactionId, data, "처리가 완료되었습니다.", HttpStatus.OK);
    }
    
    @Override
    public void setSuccessResult(String transactionId, T data, String message, HttpStatus status) {
        DeferredResult<ResponseEntity<ApiResponse<T>>> result = pendingResults.get(transactionId);
        if (result != null && !result.isSetOrExpired()) {
            log.info("트랜잭션 {} 성공 결과 설정", transactionId);
            ResponseEntity<ApiResponse<T>> responseEntity = 
                    ResponseEntity.ok(ApiResponse.success(data, message, status));
            result.setResult(responseEntity);
        } else {
            log.warn("트랜잭션 {}에 대한 DeferredResult를 찾을 수 없거나 이미 완료됨", transactionId);
        }
    }
    
    @Override
    public void setErrorResult(String transactionId, String errorMessage, HttpStatus status) {
        DeferredResult<ResponseEntity<ApiResponse<T>>> result = pendingResults.get(transactionId);
        if (result != null && !result.isSetOrExpired()) {
            log.info("트랜잭션 {} 오류 결과 설정: {}", transactionId, errorMessage);
            ResponseEntity<ApiResponse<T>> responseEntity = 
                    ResponseEntity.status(status)
                            .body(ApiResponse.fail(errorMessage, status));
            result.setResult(responseEntity);
        } else {
            log.warn("트랜잭션 {}에 대한 DeferredResult를 찾을 수 없거나 이미 완료됨", transactionId);
        }
    }
}
