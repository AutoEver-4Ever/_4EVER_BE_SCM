package org.ever._4ever_be_scm.infrastructure.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ever._4ever_be_scm.common.async.GenericAsyncResultManager;
import org.ever._4ever_be_scm.scm.iv.entity.ProductStock;
import org.ever._4ever_be_scm.scm.iv.repository.ProductStockRepository;
import org.ever.event.SalesOrderStatusChangeCompletionEvent;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.ever._4ever_be_scm.infrastructure.kafka.config.KafkaTopicConfig.SALES_ORDER_STATUS_CHANGE_COMPLETION_TOPIC;

/**
 * 판매주문 상태 변경 완료 이벤트 리스너
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SalesOrderStatusChangeCompletionListener {

    private final GenericAsyncResultManager<Void> asyncResultManager;
    private final ProductStockRepository productStockRepository;

    @KafkaListener(topics = SALES_ORDER_STATUS_CHANGE_COMPLETION_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void handleSalesOrderStatusChangeCompletion(SalesOrderStatusChangeCompletionEvent event, Acknowledgment acknowledgment) {
        log.info("판매주문 상태 변경 완료 이벤트 수신: transactionId={}, salesOrderId={}, success={}",
                event.getTransactionId(), event.getSalesOrderId(), event.isSuccess());

        try {
            if (event.isSuccess()) {
                // 1. 각 itemId에 대해 예약 재고 해제 및 forShipmentCount 감소
                if (event.getItemIds() != null && !event.getItemIds().isEmpty()) {
                    for (String itemId : event.getItemIds()) {
                        ProductStock productStock = productStockRepository.findByProductId(itemId)
                                .orElse(null);

                        if (productStock != null) {
                            // 현재 재고 상태 로깅용
                            BigDecimal currentReserved = productStock.getReservedCount() != null ?
                                    productStock.getReservedCount() : BigDecimal.ZERO;
                            BigDecimal currentForShipmentCount = productStock.getForShipmentCount() != null ?
                                    productStock.getForShipmentCount() : BigDecimal.ZERO;

                            // 1) 예약 재고 해제 (예약된 재고가 있으면 먼저 해제)
                            if (currentReserved.compareTo(BigDecimal.ZERO) > 0) {
                                BigDecimal releaseAmount = currentReserved.min(BigDecimal.ONE);
                                productStock.releaseReservation(releaseAmount);
                                log.info("예약 재고 해제: productId={}, 해제량={}, reservedCount: {} → {}",
                                        itemId, releaseAmount, currentReserved, productStock.getReservedCount());
                            }

                            // 2) forShipmentCount 감소
                            if (currentForShipmentCount.compareTo(BigDecimal.ONE) >= 0) {
                                BigDecimal newForShipmentCount = currentForShipmentCount.subtract(BigDecimal.ONE);
                                productStock.setForShipmentCount(newForShipmentCount);
                                log.info("forShipmentCount 감소: productId={}, 기존={}, 신규={}",
                                        itemId, currentForShipmentCount, newForShipmentCount);
                            } else {
                                log.warn("forShipmentCount가 부족합니다: productId={}, 현재={}", itemId, currentForShipmentCount);
                            }

                            // 3) DB 저장
                            productStockRepository.save(productStock);

                        } else {
                            log.warn("ProductStock을 찾을 수 없습니다: productId={}", itemId);
                        }
                    }
                }

                // 2. 성공 결과 설정
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
