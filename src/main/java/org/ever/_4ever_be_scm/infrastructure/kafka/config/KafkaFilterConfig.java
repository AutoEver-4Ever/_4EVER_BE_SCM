package org.ever._4ever_be_scm.infrastructure.kafka.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.adapter.RecordFilterStrategy;

/**
 * Kafka 메시지 필터 설정
 * 필요한 경우 이 파일의 주석을 해제하여 사용
 */
@Slf4j
@Configuration
public class KafkaFilterConfig {

    /**
     * 결제 필터 - 특정 조건의 메시지만 처리
     *
     * 예: 금액이 일정 이상인 결제만 처리하거나,
     *     특정 상태의 결제만 처리하고 싶을 때 사용
     */
    @Bean
    public RecordFilterStrategy<String, Object> paymentFilter() {
        return new RecordFilterStrategy<String, Object>() {
            @Override
            public boolean filter(ConsumerRecord<String, Object> record) {
                // true를 반환하면 메시지가 필터링됨 (처리하지 않음)
                // false를 반환하면 메시지가 처리됨

                // 예시 1: null 값 필터링
                if (record.value() == null) {
                    log.warn("토픽에서 null 메시지 필터링됨: {}", record.topic());
                    return true;
                }

                // 예시 2: 특정 키 패턴 필터링
                if (record.key() != null && record.key().startsWith("test-")) {
                    log.debug("테스트 메시지 필터링됨: {}", record.key());
                    return true;
                }

                // 예시 3: 비즈니스 로직 기반 필터링
                // PaymentEvent event = (PaymentEvent) record.value();
                // if (event.getAmount().compareTo(BigDecimal.valueOf(100)) < 0) {
                //     return true;
                // }

                return false; // 필터링하지 않음 (메시지 처리)
            }
        };
    }

    /**
     * 다른 필터 예시
     */
    // @Bean
    // public RecordFilterStrategy<String, Object> highValuePaymentFilter() {
    //     return record -> {
    //         // 고액 결제만 처리하는 필터
    //         if (record.value() instanceof PaymentEvent) {
    //             PaymentEvent event = (PaymentEvent) record.value();
    //             return event.getAmount().compareTo(BigDecimal.valueOf(1000000)) < 0;
    //         }
    //         return false;
    //     };
    // }
}
