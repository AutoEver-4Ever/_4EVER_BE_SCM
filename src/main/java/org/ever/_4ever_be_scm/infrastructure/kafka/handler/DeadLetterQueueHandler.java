package org.ever._4ever_be_scm.infrastructure.kafka.handler;

import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * Dead Letter Queue 처리 인터페이스
 */
public interface DeadLetterQueueHandler {

    /**
     * Dead Letter Queue로 메시지 전송
     */
    void sendToDeadLetterQueue(ConsumerRecord<?, ?> record, Exception exception);

    /**
     * 재시도 로직
     */
    boolean shouldRetry(Exception exception, int attemptCount, int maxAttempts);
}
