package org.ever._4ever_be_scm.common.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ever._4ever_be_scm.common.audit.EntityAuditListener;
import org.ever._4ever_be_scm.common.audit.TransactionEntityChangeCollector;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Saga 트랜잭션을 관리하는 서비스
 * 트랜잭션 시작, 변경 내역 저장, 롤백 처리 등을 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class  SagaTransactionManager {

    private final SagaCompensationService compensationService;
    private final TransactionEntityChangeCollector changeCollector;

    /**
     * Saga 트랜잭션을 시작하고 작업을 수행한 후 결과를 반환
     * @param <T> 반환 값 타입
     * @param action 실행할 액션
     * @return 액션 실행 결과
     */
    @Transactional
    public <T> T executeSaga(Supplier<T> action) {
        return executeSagaWithId(null, action);
    }
    
    /**
     * 외부에서 제공된 트랜잭션 ID로 Saga 트랜잭션을 실행
     * @param <T> 반환 값 타입
     * @param externalTransactionId 외부에서 제공된 트랜잭션 ID (없으면 새로 생성)
     * @param action 실행할 액션
     * @return 액션 실행 결과
     */
    @Transactional
    public <T> T executeSagaWithId(String externalTransactionId, Supplier<T> action) {
        String transactionId = (externalTransactionId != null) ? externalTransactionId : UUID.randomUUID().toString();
        try {
            // 트랜잭션 ID를 ThreadLocal에 설정
            SagaTransactionContext.setCurrentTransactionId(transactionId);
            log.info("Starting saga transaction: {}", transactionId);
            
            // 액션 수행
            T result = action.get();
            
            // 변경 내역 영구 저장
            changeCollector.persistChanges(transactionId);
            log.info("Successfully completed saga transaction: {}", transactionId);
            
            return result;
        } catch (Exception e) {
            log.error("Saga transaction {} failed, initiating compensation: {}", transactionId, e.getMessage());
            // 롤백 수행
            try {
                compensationService.compensate(transactionId);
            } catch (Exception ex) {
                log.error("Compensation for transaction {} failed: {}", transactionId, ex.getMessage());
            }
            
            // 원래 예외를 다시 throw
            throw e;
        } finally {
            // 변경 내역 캐시 정리
            changeCollector.clearChanges(transactionId);
            // ThreadLocal 정리
            SagaTransactionContext.clear();
            EntityAuditListener.clearOriginalEntityStates();
        }
    }
    
    /**
     * 반환 값이 없는 액션을 위한 오버로드된 메소드
     * @param action 실행할 액션
     */
    @Transactional
    public void executeSaga(Runnable action) {
        executeSaga(() -> {
            action.run();
            return null;
        });
    }
    
    /**
     * 외부에서 제공된 트랜잭션 ID로 반환 값이 없는 액션을 실행
     * @param externalTransactionId 외부에서 제공된 트랜잭션 ID
     * @param action 실행할 액션
     */
    @Transactional
    public void executeSagaWithId(String externalTransactionId, Runnable action) {
        executeSagaWithId(externalTransactionId, () -> {
            action.run();
            return null;
        });
    }
    
    /**
     * 외부에서 롤백 요청이 왔을 때 해당 트랜잭션 ID로 보상 트랜잭션 실행
     * @param transactionId 롤백할 트랜잭션 ID
     * @return 롤백 성공 여부
     */
    @Transactional
    public boolean compensateExternalTransaction(String transactionId) {
        if (transactionId == null || transactionId.isEmpty()) {
            log.error("Invalid transaction ID for compensation");
            return false;
        }
        
        log.info("Processing external compensation request for transaction: {}", transactionId);
        try {
            compensationService.compensate(transactionId);
            log.info("Successfully compensated external transaction: {}", transactionId);
            return true;
        } catch (Exception e) {
            log.error("Failed to compensate external transaction {}: {}", transactionId, e.getMessage(), e);
            return false;
        }
    }
}
