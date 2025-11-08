package org.ever._4ever_be_scm.infrastructure.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ever._4ever_be_scm.common.async.GenericAsyncResultManager;
import org.ever.event.PurchaseOrderApprovalCompletionEvent;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import static org.ever._4ever_be_scm.infrastructure.kafka.config.KafkaTopicConfig.PURCHASE_ORDER_APPROVAL_COMPLETION_TOPIC;

/**
 * 구매주문 승인 완료 이벤트 리스너
 * Business에서 PurchaseVoucher 생성 완료 후 DeferredResult 설정
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PurchaseOrderApprovalCompletionListener {

    private final GenericAsyncResultManager<Void> asyncResultManager;

    @KafkaListener(topics = PURCHASE_ORDER_APPROVAL_COMPLETION_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    public void handlePurchaseOrderApprovalCompletion(PurchaseOrderApprovalCompletionEvent event, Acknowledgment acknowledgment) {
        log.info("구매주문 승인 완료 이벤트 수신: transactionId={}, purchaseOrderId={}, success={}",
                event.getTransactionId(), event.getPurchaseOrderId(), event.isSuccess());

        try {
            if (event.isSuccess()) {
                // 성공 결과 설정
                asyncResultManager.setSuccessResult(
                        event.getTransactionId(),
                        null,
                        "발주서 승인이 완료되었습니다.",
                        HttpStatus.OK
                );
                log.info("구매주문 승인 성공: transactionId={}, purchaseOrderId={}",
                        event.getTransactionId(), event.getPurchaseOrderId());
            } else {
                // 실패 결과 설정
                asyncResultManager.setErrorResult(
                        event.getTransactionId(),
                        "구매주문 승인 실패: " + event.getErrorMessage(),
                        HttpStatus.INTERNAL_SERVER_ERROR
                );
                log.error("구매주문 승인 실패: transactionId={}, purchaseOrderId={}, error={}",
                        event.getTransactionId(), event.getPurchaseOrderId(), event.getErrorMessage());
            }

            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("구매주문 승인 완료 이벤트 처리 실패: transactionId={}, purchaseOrderId={}",
                    event.getTransactionId(), event.getPurchaseOrderId(), e);
            acknowledgment.acknowledge();
        }
    }
}
