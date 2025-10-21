package org.ever._4ever_be_scm.common.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 단일 엔티티의 변경 내역을 표현하는 클래스
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntityChange {
    /**
     * 엔티티 타입 (보통 클래스명이나 테이블명)
     */
    private String entityType;
    
    /**
     * 엔티티 ID
     */
    private String entityId;
    
    /**
     * 변경 유형 (CREATED, UPDATED, DELETED)
     */
    private String changeType;
    
    /**
     * 변경 전 상태 (JSON 직렬화를 위해 Object 타입으로 저장)
     */
    private Object previousState;
    
    /**
     * 변경 후 상태 (JSON 직렬화를 위해 Object 타입으로 저장)
     */
    private Object currentState;
}
