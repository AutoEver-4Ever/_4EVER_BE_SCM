package org.ever._4ever_be_scm.infrastructure.kafka.producer;

import java.util.concurrent.CompletableFuture;
import org.ever._4ever_be_scm.infrastructure.kafka.event.BusinessEvent;
import org.ever._4ever_be_scm.infrastructure.kafka.event.ScmEvent;
import org.ever._4ever_be_scm.infrastructure.kafka.event.UserEvent;
import org.ever.event.AlarmEvent;
import org.springframework.kafka.support.SendResult;

/**
 * Kafka Producer 서비스 인터페이스
 */
public interface KafkaProducerService {

    /**
     * SCM 이벤트 발행 (특정 토픽 지정)
     */
    CompletableFuture<SendResult<String, Object>> sendScmEventToTopic(String topic, ScmEvent event);

    /**
     * User 서비스로 이벤트 발행
     */
    CompletableFuture<SendResult<String, Object>> sendUserEvent(UserEvent event);

    /**
     * SCM 서비스로 이벤트 발행 (기본 토픽)
     */
    CompletableFuture<SendResult<String, Object>> sendScmEvent(ScmEvent event);

    /**
     * Business 서비스로 이벤트 발행
     */
    CompletableFuture<SendResult<String, Object>> sendBusinessEvent(BusinessEvent event);

    /**
     * Alarm 서비스로 이벤트 발행
     */
    CompletableFuture<SendResult<String, Object>> sendAlarmEvent(AlarmEvent event);

    /**
     * 동기 방식 이벤트 발행
     */
    void sendEventSync(String topic, String key, Object event);

    /**
     * 특정 토픽으로 이벤트 발행 (범용)
     */
    CompletableFuture<SendResult<String, Object>> sendToTopic(String topic, String key,
        Object event);
}
