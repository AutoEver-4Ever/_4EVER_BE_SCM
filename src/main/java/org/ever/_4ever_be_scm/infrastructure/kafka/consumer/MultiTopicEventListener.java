package org.ever._4ever_be_scm.infrastructure.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ever._4ever_be_scm.infrastructure.kafka.consumer.handler.MultiTopicEventHandler;
import org.ever._4ever_be_scm.infrastructure.kafka.event.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import static org.ever._4ever_be_scm.infrastructure.kafka.config.KafkaTopicConfig.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class MultiTopicEventListener {

    private final ObjectMapper objectMapper;
    private final MultiTopicEventHandler multiTopicEventHandler;

    /**
     * 여러 토픽을 동시에 구독하는 Multi-topic Listener
     */
    @KafkaListener(
        topics = {USER_EVENT_TOPIC, SCM_EVENT_TOPIC, BUSINESS_EVENT_TOPIC, ALARM_EVENT_TOPIC},
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleMultiTopicEvents(
        @Payload String message,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        Acknowledgment acknowledgment
    ) {
        try {
            log.info("멀티 토픽 메시지 수신 - Topic: {}, Partition: {}, Offset: {}",
                topic, partition, offset);

            // 토픽별로 다른 처리
            switch (topic) {
                case USER_EVENT_TOPIC:
                    handleUserEvent(message);
                    break;
                case SCM_EVENT_TOPIC:
                    handleScmEvent(message);
                    break;
                case BUSINESS_EVENT_TOPIC:
                    handleBusinessEvent(message);
                    break;
                case ALARM_EVENT_TOPIC:
                    handleAlarmEvent(message);
                    break;
                default:
                    log.warn("알 수 없는 토픽 - Topic: {}", topic);
            }

            acknowledgment.acknowledge();
            log.info("멀티 토픽 메시지 처리 완료 - Topic: {}", topic);

        } catch (Exception e) {
            log.error("멀티 토픽 메시지 처리 실패 - Topic: {}", topic, e);
        }
    }

    private void handleUserEvent(String message) {
        try {
            UserEvent event = objectMapper.readValue(message, UserEvent.class);
            log.info("사용자 이벤트 처리 중 - UserId: {}, Action: {}", event.getUserId(), event.getAction());

            // User 이벤트 처리 로직
            multiTopicEventHandler.handleUserEvent(event);

        } catch (Exception e) {
            log.error("사용자 이벤트 파싱 실패", e);
        }
    }

    private void handleScmEvent(String message) {
        try {
            ScmEvent event = objectMapper.readValue(message, ScmEvent.class);
            log.info("SCM 이벤트 처리 중 - OrderId: {}, Action: {}", event.getOrderId(), event.getAction());

            // SCM 이벤트 처리 로직
            multiTopicEventHandler.handleScmEvent(event);

        } catch (Exception e) {
            log.error("SCM 이벤트 파싱 실패", e);
        }
    }

    private void handleBusinessEvent(String message) {
        try {
            BusinessEvent event = objectMapper.readValue(message, BusinessEvent.class);
            log.info("비즈니스 이벤트 처리 중 - BusinessId: {}, Action: {}",
                event.getBusinessId(), event.getAction());

            // Business 이벤트 처리 로직
            multiTopicEventHandler.handleBusinessEvent(event);

        } catch (Exception e) {
            log.error("비즈니스 이벤트 파싱 실패", e);
        }
    }

    private void handleAlarmEvent(String message) {
        try {
            AlarmEvent event = objectMapper.readValue(message, AlarmEvent.class);
            log.info("알림 이벤트 처리 중 - UserId: {}, Type: {}", event.getUserId(), event.getAlarmType());

            // Alarm 이벤트 처리 로직
            multiTopicEventHandler.handleAlarmEvent(event);

        } catch (Exception e) {
            log.error("알림 이벤트 파싱 실패", e);
        }
    }
}
