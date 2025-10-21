package org.ever._4ever_be_scm.common.saga;

/**
 * 보상 트랜잭션 처리를 위한 인터페이스
 * 각 엔티티 타입별로 구현하여 롤백 방식을 정의
 */
public interface CompensationHandler {
    /**
     * 엔티티를 이전 상태로 복원
     * @param entity 이전 상태의 엔티티 객체
     */
    void restore(Object entity);
    
    /**
     * 엔티티 삭제 (CREATE 작업의 보상)
     * @param entityId 삭제할 엔티티의 ID
     */
    void delete(String entityId);
    
    /**
     * 처리 대상 엔티티 클래스 반환
     * @return 엔티티의 Java 클래스
     */
    Class<?> getEntityClass();
}
