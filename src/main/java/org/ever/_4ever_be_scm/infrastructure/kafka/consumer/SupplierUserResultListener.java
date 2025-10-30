package org.ever._4ever_be_scm.infrastructure.kafka.consumer;

import com.github.f4b6a3.uuid.UuidCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ever._4ever_be_scm.common.async.AsyncResultManager;
import org.ever._4ever_be_scm.infrastructure.kafka.config.KafkaTopicConfig;
import org.ever._4ever_be_scm.infrastructure.kafka.producer.KafkaProducerService;
import org.ever.event.CreateAuthUserResultEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "external.scm.supplier-user.adapter", havingValue = "kafka", matchIfMissing = true)
public class SupplierUserResultListener {

    private final AsyncResultManager<CreateAuthUserResultEvent> asyncResultManager;
    private final KafkaProducerService kafkaProducerService;

    @KafkaListener(
        topics = KafkaTopicConfig.SUPPLIER_USER_RESULT_TOPIC,
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleSupplierUserResult(
        CreateAuthUserResultEvent event,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        Acknowledgment acknowledgment
    ) {
        log.info("[KAFKA][SUPPLIER] 사용자 생성 결과 수신 - topic: {}, partition: {}, offset: {}, txId: {}, success: {}",
            topic, partition, offset, event.getTransactionId(), event.isSuccess());

        String transactionId = event.getTransactionId();
        if (transactionId == null) {
            log.error("[KAFKA][SUPPLIER][ERROR] transactionId 가 없는 결과 이벤트: {}", event);
            acknowledgment.acknowledge();
            return;
        }

        if (!asyncResultManager.hasPendingResult(transactionId)) {
            log.warn("[KAFKA][SUPPLIER][WARN] 처리할 DeferredResult가 없어 결과 이벤트를 무시합니다. transactionId: {}", transactionId);
            acknowledgment.acknowledge();
            return;
        }

        try {
            if (event.isSuccess()) {
                asyncResultManager.setSuccessResult(
                    transactionId,
                    event,
                    "[SAGA][SUCCESS] 공급사 사용자 생성이 완료되었습니다.",
                    HttpStatus.CREATED
                );
                log.info("[KAFKA][SUPPLIER] 사용자 생성 성공 처리 완료 - transactionId: {}", transactionId);
            } else {
                asyncResultManager.setErrorResult(
                    transactionId,
                    "[SAGA][FAIL] 공급사 사용자 생성 실패: " + event.getFailureReason(),
                    HttpStatus.INTERNAL_SERVER_ERROR
                );
                publishRollbackEvent(event);
            }

            acknowledgment.acknowledge();
        } catch (Exception ex) {
            log.error("[KAFKA][SUPPLIER][ERROR] 사용자 생성 결과 처리 중 오류 - transactionId: {}", transactionId, ex);
            asyncResultManager.setErrorResult(
                transactionId,
                "[SAGA][FAIL] 결과 처리 중 오류가 발생했습니다: " + ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    private void publishRollbackEvent(CreateAuthUserResultEvent event) {
        CreateAuthUserResultEvent rollbackEvent = CreateAuthUserResultEvent.builder()
            .eventId(String.valueOf(UuidCreator.getTimeOrderedEpoch()))
            .transactionId(event.getTransactionId())
            .success(false)
            .userId(event.getUserId())
            .failureReason(event.getFailureReason())
            .build();

        kafkaProducerService.sendEventSync(
            KafkaTopicConfig.USER_ROLLBACK_TOPIC,
            rollbackEvent.getTransactionId(),
            rollbackEvent
        );
        log.info("[KAFKA][SUPPLIER] 사용자 생성 실패 보상 이벤트 발행 - transactionId: {}", rollbackEvent.getTransactionId());
    }
}
