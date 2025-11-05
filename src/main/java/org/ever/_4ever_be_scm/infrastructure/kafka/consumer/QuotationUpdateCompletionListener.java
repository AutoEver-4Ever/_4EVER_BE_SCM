package org.ever._4ever_be_scm.infrastructure.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ever._4ever_be_scm.common.async.GenericAsyncResultManager;
import org.ever.event.QuotationUpdateCompletionEvent;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import static org.ever._4ever_be_scm.infrastructure.kafka.config.KafkaTopicConfig.QUOTATION_UPDATE_COMPLETION_TOPIC;

/**
 * 견적 업데이트 완료 이벤트 리스너
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QuotationUpdateCompletionListener {

    private final GenericAsyncResultManager<Void> asyncResultManager;

    @KafkaListener(topics = QUOTATION_UPDATE_COMPLETION_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    public void handleQuotationUpdateCompletion(QuotationUpdateCompletionEvent event, Acknowledgment acknowledgment) {
        log.info("견적 업데이트 완료 이벤트 수신: transactionId={}, quotationId={}, success={}",
                event.getTransactionId(), event.getQuotationId(), event.isSuccess());

        try {
            if (event.isSuccess()) {
                // 성공 결과 설정
                asyncResultManager.setSuccessResult(
                        event.getTransactionId(),
                        null,
                        "견적 확정이 완료되었습니다.",
                        HttpStatus.OK
                );
                log.info("견적 확정 성공: transactionId={}, quotationId={}",
                        event.getTransactionId(), event.getQuotationId());
            } else {
                // 실패 결과 설정
                asyncResultManager.setErrorResult(
                        event.getTransactionId(),
                        "견적 확정 실패: " + event.getErrorMessage(),
                        HttpStatus.INTERNAL_SERVER_ERROR
                );
                log.error("견적 확정 실패: transactionId={}, quotationId={}, error={}",
                        event.getTransactionId(), event.getQuotationId(), event.getErrorMessage());
            }

            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("견적 업데이트 완료 이벤트 처리 실패: transactionId={}, quotationId={}",
                    event.getTransactionId(), event.getQuotationId(), e);
            acknowledgment.acknowledge();
        }
    }
}
