package org.ever._4ever_be_scm.common.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ever._4ever_be_scm.common.entity.TransactionChangeLog;
import org.ever._4ever_be_scm.common.repository.TransactionChangeLogRepository;
import org.ever._4ever_be_scm.common.saga.SagaTransactionContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 트랜잭션 중에 발생한 엔티티 변경 사항을 수집하는 컴포넌트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionEntityChangeCollector {

    private final TransactionChangeLogRepository changeLogRepository;
    private final ObjectMapper objectMapper;
    private final Map<String, TransactionChanges> changesByTransaction = new ConcurrentHashMap<>();

    /**
     * 엔티티 변경 사항 기록
     * @param entityType 엔티티 타입
     * @param entityId 엔티티 ID
     * @param changeType 변경 타입 (생성, 수정, 삭제)
     * @param previousState 변경 전 상태
     * @param currentState 변경 후 상태
     */
    public void recordChange(String entityType, String entityId, ChangeType changeType, 
                             Object previousState, Object currentState) {
        
        String transactionId = SagaTransactionContext.getCurrentTransactionId();
        if (transactionId == null) {
            log.warn("No active saga transaction found for entity change: {} - {}", entityType, entityId);
            return;
        }
        
        EntityChange change = EntityChange.builder()
                .entityType(entityType)
                .entityId(entityId)
                .changeType(changeType.name())
                .previousState(previousState)
                .currentState(currentState)
                .build();
        
        changesByTransaction.computeIfAbsent(transactionId, k -> new TransactionChanges()).addChange(change);
        
        log.debug("Recorded {} change for entity type: {}, id: {}, transaction: {}", 
                changeType, entityType, entityId, transactionId);
    }
    
    /**
     * 변경 사항을 데이터베이스에 영구 저장
     * @param transactionId 트랜잭션 ID
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void persistChanges(String transactionId) {
        TransactionChanges changes = changesByTransaction.remove(transactionId);
        if (changes != null && !changes.getChanges().isEmpty()) {
            try {
                String changesJson = objectMapper.writeValueAsString(changes);
                
                TransactionChangeLog changeLog = TransactionChangeLog.builder()
                        .changesJson(changesJson)
                        .timestamp(LocalDateTime.now())
                        .compensated(false)
                        .build();

                changeLog.assignTransactionId(transactionId);
                
                changeLogRepository.save(changeLog);
                log.debug("Persisted changes for transaction {}", transactionId);
            } catch (Exception e) {
                log.error("Failed to persist transaction changes: {}", e.getMessage(), e);
            }
        }
    }
    
    /**
     * 트랜잭션 변경 내역 캐시 정리
     * @param transactionId 트랜잭션 ID
     */
    public void clearChanges(String transactionId) {
        changesByTransaction.remove(transactionId);
        log.debug("Cleared change logs for transaction {}", transactionId);
    }
}
