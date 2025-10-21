package org.ever._4ever_be_scm.common.audit;

/**
 * 엔티티 변경 유형을 정의하는 열거형
 */
public enum ChangeType {
    /**
     * 엔티티 생성
     */
    CREATED,
    
    /**
     * 엔티티 수정
     */
    UPDATED,
    
    /**
     * 엔티티 삭제
     */
    DELETED
}
