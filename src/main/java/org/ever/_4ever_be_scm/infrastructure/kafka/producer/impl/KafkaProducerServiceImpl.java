package org.ever._4ever_be_scm.infrastructure.kafka.producer.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ever._4ever_be_scm.infrastructure.kafka.event.*;
import org.ever._4ever_be_scm.infrastructure.kafka.producer.KafkaProducerService;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

import static org.ever._4ever_be_scm.infrastructure.kafka.config.KafkaTopicConfig.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerServiceImpl implements KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public CompletableFuture<SendResult<String, Object>> sendScmEventToTopic(String topic, ScmEvent event) {
        return sendEvent(topic, event.getOrderId(), event);
    }

    @Override
    public CompletableFuture<SendResult<String, Object>> sendUserEvent(UserEvent event) {
        return sendEvent(USER_EVENT_TOPIC, event.getUserId(), event);
    }

    @Override
    public CompletableFuture<SendResult<String, Object>> sendScmEvent(ScmEvent event) {
        return sendEvent(SCM_EVENT_TOPIC, event.getOrderId(), event);
    }

    @Override
    public CompletableFuture<SendResult<String, Object>> sendBusinessEvent(BusinessEvent event) {
        return sendEvent(BUSINESS_EVENT_TOPIC, event.getBusinessId(), event);
    }

    @Override
    public CompletableFuture<SendResult<String, Object>> sendAlarmEvent(AlarmEvent event) {
        return sendEvent(ALARM_EVENT_TOPIC, event.getUserId(), event);
    }

    @Override
    public void sendEventSync(String topic, String key, Object event) {
        try {
            SendResult<String, Object> result = sendEvent(topic, key, event).get();
            log.info("동기 이벤트 발행 완료 - Topic: {}, Partition: {}, Offset: {}",
                topic, result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
        } catch (Exception e) {
            log.error("동기 이벤트 발행 실패 - Topic: {}, Key: {}", topic, key, e);
            throw new RuntimeException("Kafka 메시지 전송 실패", e);
        }
    }

    /**
     * 공통 이벤트 발행 메서드
     */
    private CompletableFuture<SendResult<String, Object>> sendEvent(String topic, String key, Object event) {
        log.info("이벤트 발행 시작 - Topic: {}, Key: {}, EventType: {}",
            topic, key, event.getClass().getSimpleName());

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("이벤트 발행 실패 - Topic: {}, Key: {}, Error: {}",
                    topic, key, ex.getMessage(), ex);
            } else {
                log.info("이벤트 발행 성공 - Topic: {}, Partition: {}, Offset: {}",
                    topic, result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
            }
        });

        return future;
    }
}
