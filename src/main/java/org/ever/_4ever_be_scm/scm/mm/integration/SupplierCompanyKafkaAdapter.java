package org.ever._4ever_be_scm.scm.mm.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ever._4ever_be_scm.infrastructure.kafka.producer.KafkaProducerService;
import org.ever._4ever_be_scm.scm.iv.repository.SupplierCompanyRepository;
import org.ever._4ever_be_scm.scm.iv.repository.SupplierUserRepository;
import org.ever.event.SupplierCompanyResolveRequestEvent;
import org.ever.event.SupplierCompanyResolveResultEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static org.ever._4ever_be_scm.infrastructure.kafka.config.KafkaTopicConfig.SUPPLIER_COMPANY_RESOLVE_RESULT_TOPIC;
import static org.ever._4ever_be_scm.infrastructure.kafka.config.KafkaTopicConfig.SUPPLIER_COMPANY_RESOLVE_REQUEST_TOPIC;

@Slf4j
@Component
@RequiredArgsConstructor
public class SupplierCompanyKafkaAdapter {

    private final SupplierUserRepository supplierUserRepository;
    private final SupplierCompanyRepository supplierCompanyRepository;
    private final KafkaProducerService kafkaProducerService;

    @KafkaListener(
            topics = SUPPLIER_COMPANY_RESOLVE_REQUEST_TOPIC,
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleRequest(SupplierCompanyResolveRequestEvent event) {
        log.info("[SCM][Kafka] 공급사 정보 요청 수신 - txId={}, userId={}",
                event.getTransactionId(), event.getSupplierUserId());

        SupplierCompanyResolveResultEvent.SupplierCompanyResolveResultEventBuilder builder =
                SupplierCompanyResolveResultEvent.builder()
                        .transactionId(event.getTransactionId())
                        .supplierUserId(event.getSupplierUserId())
                        .timestamp(System.currentTimeMillis());

        try {
            var supplierUser = supplierUserRepository.findByUserId(event.getSupplierUserId())
                    .orElseThrow(() -> new IllegalArgumentException("supplier user not found"));

            var supplierCompany = supplierCompanyRepository.findBySupplierUser(supplierUser)
                    .orElseThrow(() -> new IllegalArgumentException("supplier company not found"));

            builder.success(true)
                    .supplierCompanyId(supplierCompany.getId())
                    .supplierCompanyName(supplierCompany.getCompanyName());

            log.info("[SCM][Kafka] 공급사 정보 조회 성공 - userId={}, companyId={}",
                    event.getSupplierUserId(), supplierCompany.getId());
        } catch (Exception e) {
            builder.success(false)
                    .errorMessage(e.getMessage());
            log.error("[SCM][Kafka] 공급사 정보 조회 실패 - userId={}, error={}",
                    event.getSupplierUserId(), e.getMessage());
        }

        kafkaProducerService.sendToTopic(
                SUPPLIER_COMPANY_RESOLVE_RESULT_TOPIC,
                event.getSupplierUserId(),
                builder.build()
        );
    }
}
