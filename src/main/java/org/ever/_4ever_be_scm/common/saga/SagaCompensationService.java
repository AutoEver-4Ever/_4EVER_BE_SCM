package org.ever._4ever_be_scm.common.saga;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ever._4ever_be_scm.common.audit.EntityChange;
import org.ever._4ever_be_scm.common.audit.TransactionChanges;
import org.ever._4ever_be_scm.common.repository.TransactionChangeLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Saga 패턴의 보상 트랜잭션을 실행하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SagaCompensationService {

    private final TransactionChangeLogRepository changeLogRepository;
    private final ObjectMapper objectMapper;
    private final Map<String, CompensationHandler> compensationHandlers = new ConcurrentHashMap<>();

    /**
     * 보상 트랜잭션 핸들러 등록
     * @param entityType 엔티티 타입
     * @param handler 핸들러 구현체
     */
    public void registerCompensationHandler(String entityType, CompensationHandler handler) {
        compensationHandlers.put(entityType, handler);
        log.info("Registered compensation handler for entity type: {}", entityType);
    }

    /**
     * 트랜잭션에 대한 보상 트랜잭션 실행
     * @param transactionId 롤백할 트랜잭션 ID
     */
    @Transactional
    public void compensate(String transactionId) {
        log.info("Starting compensation for transaction: {}", transactionId);
        
        changeLogRepository.findByTransactionId(transactionId).ifPresent(logEntry -> {
            try {
                if (logEntry.isCompensated()) {
                    log.info("Transaction {} already compensated", transactionId);
                    return;
                }
                
                TransactionChanges changes = objectMapper.readValue(
                        logEntry.getChangesJson(),
                        objectMapper.getTypeFactory().constructType(TransactionChanges.class));
                
                // 변경 내역을 역순으로 처리 (LIFO 방식으로 보상)
                for (int i = changes.getChanges().size() - 1; i >= 0; i--) {
                    EntityChange change = changes.getChanges().get(i);
                    compensateChange(change);
                }
                
                // 보상 처리 완료 표시
                changeLogRepository.markAsCompensated(transactionId);
                log.info("Compensation completed for transaction: {}", transactionId);
            } catch (Exception e) {
                log.error("Error during compensation of transaction {}: {}", transactionId, e.getMessage(), e);
                throw new RuntimeException("Compensation failed for transaction: " + transactionId, e);
            }
        });
    }

    /**
     * 개별 엔티티 변경에 대한 보상 처리
     * @param change 보상할 변경 사항
     * @throws Exception JSON 변환 등 예외 발생 시
     */
    private void compensateChange(EntityChange change) throws Exception {
        CompensationHandler handler = compensationHandlers.get(change.getEntityType());
        if (handler == null) {
            log.warn("No compensation handler found for entity type: {}", change.getEntityType());
            return;
        }
        
        JavaType entityType = objectMapper.getTypeFactory().constructType(handler.getEntityClass());
        log.info("Compensating {} operation on {} with id {}", 
                change.getChangeType(), change.getEntityType(), change.getEntityId());
        
        switch (change.getChangeType()) {
            case "CREATED":
                handler.delete(change.getEntityId());
                break;
                
            case "UPDATED":
                if (change.getPreviousState() != null) {
                    Object previousState = objectMapper.convertValue(change.getPreviousState(), entityType);
                    handler.restore(previousState);
                }
                break;
                
            case "DELETED":
                if (change.getPreviousState() != null) {
                    Object deletedEntity = objectMapper.convertValue(change.getPreviousState(), entityType);
                    handler.restore(deletedEntity);
                }
                break;
        }
    }
}
