package org.ever._4ever_be_scm.common.audit;

/**
 * 자동 감사 기능을 적용할 엔티티에 구현해야 하는 인터페이스
 * 이 인터페이스를 구현한 엔티티는 EntityAuditListener에 의해 자동으로 변경사항이 추적됨
 */
public interface Auditable {
    /**
     * 엔티티의 ID 값을 문자열로 반환
     * @return ID 값 문자열
     */
    String getAuditableId();
    
    /**
     * 엔티티 타입을 문자열로 반환 (보통 클래스명이나 테이블명)
     * @return 엔티티 타입 문자열
     */
    String getAuditableType();
}
