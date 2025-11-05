package org.ever._4ever_be_scm.infrastructure.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ever._4ever_be_scm.common.async.GenericAsyncResultManager;
import org.ever.event.SalesOrderStatusChangeCompletionEvent;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import static org.ever._4ever_be_scm.infrastructure.kafka.config.KafkaTopicConfig.SALES_ORDER_STATUS_CHANGE_COMPLETION_TOPIC;

/**
 * 판매주문 상태 변경 완료 이벤트 리스너
 * 재고는 이미 SalesOrderServiceImpl에서 차감되었으므로,
 * 여기서는 DeferredResult만 설정한다.
 * 실패 시 재고 복구는 트랜잭션 롤백에 의해 자동 처리된다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SalesOrderStatusChangeCompletionListener {

    private final GenericAsyncResultManager<Void> asyncResultManager;

    @KafkaListener(topics = SALES_ORDER_STATUS_CHANGE_COMPLETION_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    public void handleSalesOrderStatusChangeCompletion(SalesOrderStatusChangeCompletionEvent event, Acknowledgment acknowledgment) {
        log.info("판매주문 상태 변경 완료 이벤트 수신: transactionId={}, salesOrderId={}, success={}",
                event.getTransactionId(), event.getSalesOrderId(), event.isSuccess());

        try {
            if (event.isSuccess()) {
                // 성공 결과 설정
                asyncResultManager.setSuccessResult(
                        event.getTransactionId(),
                        null,
                        "판매 주문 상태가 변경되었습니다.",
                        HttpStatus.OK
                );
                log.info("판매주문 상태 변경 성공: transactionId={}, salesOrderId={}",
                        event.getTransactionId(), event.getSalesOrderId());
            } else {
                // 실패 결과 설정
                // 재고는 트랜잭션 롤백으로 자동 복구됨
                asyncResultManager.setErrorResult(
                        event.getTransactionId(),
                        "판매주문 상태 변경 실패: " + event.getErrorMessage(),
                        HttpStatus.INTERNAL_SERVER_ERROR
                );
                log.error("판매주문 상태 변경 실패: transactionId={}, salesOrderId={}, error={}",
                        event.getTransactionId(), event.getSalesOrderId(), event.getErrorMessage());
            }

            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("판매주문 상태 변경 완료 이벤트 처리 실패: transactionId={}, salesOrderId={}",
                    event.getTransactionId(), event.getSalesOrderId(), e);
            acknowledgment.acknowledge();
        }
    }
}
