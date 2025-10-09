package org.ever._4ever_be_scm.infrastructure.kafka.handler.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.ever._4ever_be_scm.infrastructure.kafka.handler.DeadLetterQueueHandler;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeadLetterQueueHandlerImpl implements DeadLetterQueueHandler {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String DLQ_SUFFIX = ".dlq";

    @Override
    public void sendToDeadLetterQueue(ConsumerRecord<?, ?> record, Exception exception) {
        String dlqTopic = record.topic() + DLQ_SUFFIX;

        log.error("DLQ로 메시지 전송 - Topic: {}, Partition: {}, Offset: {}, Error: {}",
            record.topic(), record.partition(), record.offset(), exception.getMessage());

        try {
            kafkaTemplate.send(dlqTopic, record.key().toString(), record.value())
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("DLQ 메시지 전송 실패 - DLQ Topic: {}", dlqTopic, ex);
                    } else {
                        log.info("DLQ 메시지 전송 성공 - DLQ Topic: {}", dlqTopic);
                    }
                });
        } catch (Exception e) {
            log.error("DLQ 전송 중 예외 발생 - DLQ Topic: {}", dlqTopic, e);
        }
    }

    @Override
    public boolean shouldRetry(Exception exception, int attemptCount, int maxAttempts) {
        if (attemptCount >= maxAttempts) {
            log.warn("최대 재시도 횟수 도달 - 재시도 횟수: {}", maxAttempts);
            return false;
        }

        // 특정 예외 타입에 따라 재시도 여부 결정
        if (isRetryableException(exception)) {
            log.info("재시도 진행 중 - 현재 시도: {}/{}", attemptCount + 1, maxAttempts);
            return true;
        }

        return false;
    }

    private boolean isRetryableException(Exception exception) {
        // 재시도 가능한 예외 판단 로직
        // 예: 네트워크 오류, 일시적인 DB 연결 오류 등
        return exception instanceof org.springframework.dao.TransientDataAccessException
            || exception instanceof java.net.ConnectException;
    }
}
