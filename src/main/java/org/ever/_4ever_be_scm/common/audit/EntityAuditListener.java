package org.ever._4ever_be_scm.common.audit;

import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PreRemove;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.HashMap;
import java.util.Map;

/**
 * JPA 엔티티 이벤트를 감지하여 변경사항을 기록하는 리스너
 * EntityListeners 어노테이션을 통해 엔티티에 연결
 */
@Slf4j
@Component
public class EntityAuditListener {

    private static final ThreadLocal<Map<String, Object>> originalEntityStates = new ThreadLocal<>();
    private static TransactionEntityChangeCollector changeCollector;

    @Autowired
    public void setChangeCollector(TransactionEntityChangeCollector collector) {
        EntityAuditListener.changeCollector = collector;
    }

    /**
     * 엔티티 로드 시 원본 상태 저장
     * @param entity 로드된 엔티티
     */
    @PostLoad
    public void onPostLoad(Object entity) {
        if (entity instanceof Auditable && TransactionSynchronizationManager.isActualTransactionActive()) {
            try {
                Auditable auditable = (Auditable) entity;
                String entityId = auditable.getAuditableId();
                String entityKey = getEntityKey(entity.getClass(), entityId);
                
                Map<String, Object> states = originalEntityStates.get();
                if (states == null) {
                    states = new HashMap<>();
                    originalEntityStates.set(states);
                }
                
                Object originalState = BeanUtils.instantiateClass(entity.getClass());
                BeanUtils.copyProperties(entity, originalState);
                states.put(entityKey, originalState);
                
            } catch (Exception e) {
                log.error("Error storing original entity state: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * 엔티티 생성 이후 호출
     * @param entity 생성된 엔티티
     */
    @PostPersist
    public void onPostPersist(Object entity) {
        if (entity instanceof Auditable && TransactionSynchronizationManager.isActualTransactionActive()) {
            Auditable auditable = (Auditable) entity;
            changeCollector.recordChange(
                    auditable.getAuditableType(),
                    auditable.getAuditableId(),
                    ChangeType.CREATED,
                    null,
                    entity
            );
        }
    }

    /**
     * 엔티티 업데이트 이후 호출
     * @param entity 업데이트된 엔티티
     */
    @PostUpdate
    public void onPostUpdate(Object entity) {
        if (entity instanceof Auditable && TransactionSynchronizationManager.isActualTransactionActive()) {
            try {
                Auditable auditable = (Auditable) entity;
                String entityId = auditable.getAuditableId();
                String entityKey = getEntityKey(entity.getClass(), entityId);
                
                Map<String, Object> states = originalEntityStates.get();
                if (states != null && states.containsKey(entityKey)) {
                    Object originalState = states.get(entityKey);
                    
                    changeCollector.recordChange(
                            auditable.getAuditableType(),
                            entityId,
                            ChangeType.UPDATED,
                            originalState,
                            entity
                    );
                }
            } catch (Exception e) {
                log.error("Error recording entity update: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * 엔티티 삭제 전 호출
     * @param entity 삭제할 엔티티
     */
    @PreRemove
    public void onPreRemove(Object entity) {
        if (entity instanceof Auditable && TransactionSynchronizationManager.isActualTransactionActive()) {
            Auditable auditable = (Auditable) entity;
            changeCollector.recordChange(
                    auditable.getAuditableType(),
                    auditable.getAuditableId(),
                    ChangeType.DELETED,
                    entity,
                    null
            );
        }
    }

    private String getEntityKey(Class<?> entityClass, String entityId) {
        return entityClass.getName() + "#" + entityId;
    }

    /**
     * 쓰레드 로컬에 저장된 원본 상태 정보 정리
     */
    public static void clearOriginalEntityStates() {
        originalEntityStates.remove();
    }
}
