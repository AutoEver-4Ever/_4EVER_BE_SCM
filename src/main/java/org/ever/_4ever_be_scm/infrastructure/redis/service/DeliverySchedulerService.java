package org.ever._4ever_be_scm.infrastructure.redis.service;

import java.time.Duration;

/**
 * 배송 완료 자동 스케줄링 서비스
 * Redisson DelayedQueue를 사용하여 지연 작업 처리
 */
public interface DeliverySchedulerService {

    /**
     * 배송 완료 자동 처리 작업 스케줄링
     *
     * @param purchaseOrderId 발주서 ID
     * @param delay 지연 시간 (배송 소요 기간)
     */
    void scheduleDeliveryCompletion(String purchaseOrderId, Duration delay);

    /**
     * 스케줄링된 작업 취소
     *
     * @param purchaseOrderId 발주서 ID
     * @return 취소 성공 여부
     */
    boolean cancelScheduledDelivery(String purchaseOrderId);
}
