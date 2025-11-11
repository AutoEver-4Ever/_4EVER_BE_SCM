package org.ever._4ever_be_scm.infrastructure.kafka.consumer;

import static org.ever._4ever_be_scm.infrastructure.kafka.config.KafkaTopicConfig.ALARM_REQUEST_STATUS_TOPIC;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ever.event.StatusEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlarmStatusEventListener {

    /**
     * 알림 요청 상태(StatusEvent) 수신 전용 리스너
     * 처리 로직 없이 수신만 하고 상세 로그를 남깁니다.
     */
    @KafkaListener(
        topics = ALARM_REQUEST_STATUS_TOPIC,
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleAlarmRequestStatus(
        @Payload StatusEvent event,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        Acknowledgment acknowledgment
    ) {
        try {
            log.info("알림 요청 상태 수신 - Topic: {}, Partition: {}, Offset: {}", topic, partition,
                offset);
            log.info(
                "알림 요청 상태 상세 - correlationId: {}, entityId: {}, status: {}, errorCode: {}, message: {}, retryCount: {}",
                event.getCorrelationId(),
                event.getEntityId(),
                event.getStatus(),
                event.getFailureDetails() != null ? event.getFailureDetails().getErrorCode() : null,
                event.getFailureDetails() != null ? event.getFailureDetails().getMessage() : null,
                event.getFailureDetails() != null ? event.getFailureDetails().getRetryCount() : null
            );

            // 명시적 커밋 (수신만 수행)
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("알림 요청 상태 수신 처리 중 오류", e);
        }
    }
}


