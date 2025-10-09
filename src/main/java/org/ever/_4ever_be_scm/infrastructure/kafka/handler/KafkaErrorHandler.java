package org.ever._4ever_be_scm.infrastructure.kafka.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaErrorHandler implements CommonErrorHandler {

    @Override
    public void handleOtherException(Exception thrownException, Consumer<?, ?> consumer,
                                     MessageListenerContainer container, boolean batchListener) {
        log.error("Kafka 컨슈머 에러 발생 - GroupId: {}", consumer.groupMetadata().groupId(), thrownException);

        // 에러 발생 시 처리 로직
        // 1. 로그 기록
        // 2. 모니터링 시스템에 알림
        // 3. Dead Letter Queue로 메시지 전송 (필요시)
    }

    @Override
    public boolean handleOne(Exception thrownException, ConsumerRecord<?, ?> record,
                             Consumer<?, ?> consumer, MessageListenerContainer container) {
        log.error("레코드 처리 중 에러 발생 - Topic: {}, Partition: {}, Offset: {}",
            record.topic(), record.partition(), record.offset(), thrownException);

        // 재처리 여부 결정
        // true: 메시지 재처리 (retry)
        // false: 메시지 skip

        return false; // 메시지 스킵
    }

    @Override
    public void handleBatch(Exception thrownException,
                           ConsumerRecords<?, ?> data,
                           Consumer<?, ?> consumer,
                           MessageListenerContainer container,
                           Runnable invokeListener) {
        log.error("배치 처리 중 에러 발생 - 레코드 수: {}", data.count(), thrownException);

        // 배치 처리 중 에러 발생 시 로직
        data.forEach(record -> {
            log.error("실패한 레코드 - Topic: {}, Partition: {}, Offset: {}",
                record.topic(), record.partition(), record.offset());
        });
    }
}
