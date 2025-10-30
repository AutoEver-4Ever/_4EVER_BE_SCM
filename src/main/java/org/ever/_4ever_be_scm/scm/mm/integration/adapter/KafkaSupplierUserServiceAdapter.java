package org.ever._4ever_be_scm.scm.mm.integration.adapter;

import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ever._4ever_be_scm.infrastructure.kafka.config.KafkaTopicConfig;
import org.ever._4ever_be_scm.infrastructure.kafka.producer.KafkaProducerService;
import org.ever._4ever_be_scm.scm.mm.integration.port.SupplierUserServicePort;
import org.ever.event.CreateSupplierUserEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "external.scm.supplier-user.adapter", havingValue = "kafka", matchIfMissing = true)
public class KafkaSupplierUserServiceAdapter implements SupplierUserServicePort {

    private final KafkaProducerService kafkaProducerService;

    @Override
    public CompletableFuture<Void> createSupplierUser(CreateSupplierUserEvent event) {
        try {
            String key = event.getUserId();
            kafkaProducerService.sendEventSync(
                KafkaTopicConfig.CREATE_SUPPLIER_USER_TOPIC,
                key,
                event
            );

            log.info("[KAFKA][SUPPLIER] 공급사 사용자 생성 요청 발행 - txId: {}, userId: {}", event.getTransactionId(), event.getUserId());
            return CompletableFuture.completedFuture(null);
        } catch (Exception error) {
            log.error("[KAFKA][SUPPLIER] 공급사 사용자 생성 이벤트 발행 실패 - txId: {}, cause: {}", event.getTransactionId(), error.getMessage(), error);
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(error);
            return future;
        }
    }
}
