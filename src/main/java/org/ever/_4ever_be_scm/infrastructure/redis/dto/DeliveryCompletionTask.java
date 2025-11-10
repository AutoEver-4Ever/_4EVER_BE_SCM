package org.ever._4ever_be_scm.infrastructure.redis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 배송 완료 자동 처리 작업 DTO
 * Redis DelayedQueue에 저장되어 지연 실행됨
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryCompletionTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 발주서 ID
     */
    private String purchaseOrderId;

    /**
     * 예정 완료 시간
     */
    private LocalDateTime scheduledCompletionTime;

    /**
     * 작업 생성 시간
     */
    private LocalDateTime createdAt;
}
