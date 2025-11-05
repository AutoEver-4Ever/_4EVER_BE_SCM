package org.ever._4ever_be_scm.infrastructure.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ever._4ever_be_scm.common.async.GenericAsyncResultManager;
import org.ever._4ever_be_scm.scm.iv.entity.ProductStock;
import org.ever._4ever_be_scm.scm.iv.repository.ProductStockRepository;
import org.ever._4ever_be_scm.scm.pp.entity.Mes;
import org.ever._4ever_be_scm.scm.pp.repository.MesRepository;
import org.ever.event.MesCompleteCompletionEvent;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.ever._4ever_be_scm.infrastructure.kafka.config.KafkaTopicConfig.MES_COMPLETE_COMPLETION_TOPIC;

/**
 * MES 완료 완료 이벤트 리스너
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MesCompleteCompletionListener {

    private final GenericAsyncResultManager<Void> asyncResultManager;
    private final MesRepository mesRepository;
    private final ProductStockRepository productStockRepository;

    @KafkaListener(topics = MES_COMPLETE_COMPLETION_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void handleMesCompleteCompletion(MesCompleteCompletionEvent event, Acknowledgment acknowledgment) {
        log.info("MES 완료 완료 이벤트 수신: transactionId={}, mesId={}, success={}",
                event.getTransactionId(), event.getMesId(), event.isSuccess());

        try {
            if (event.isSuccess()) {
                // 1. MES 조회
                Mes mes = mesRepository.findById(event.getMesId()).orElse(null);
                if (mes != null) {
                    // 2. ProductStock의 forShipmentCount 증가
                    ProductStock productStock = productStockRepository.findByProductId(mes.getProductId())
                            .orElse(null);

                    if (productStock != null) {
                        BigDecimal currentForShipmentCount = productStock.getForShipmentCount() != null ?
                                productStock.getForShipmentCount() : BigDecimal.ZERO;
                        BigDecimal newForShipmentCount = currentForShipmentCount.add(BigDecimal.valueOf(mes.getQuantity()));
                        productStock.setForShipmentCount(newForShipmentCount);
                        productStockRepository.save(productStock);

                        log.info("ProductStock forShipmentCount 증가: productId={}, 기존={}, 신규={}, 증가량={}",
                                mes.getProductId(), currentForShipmentCount, newForShipmentCount, mes.getQuantity());
                    } else {
                        log.warn("ProductStock을 찾을 수 없습니다: productId={}", mes.getProductId());
                    }
                }

                // 3. 성공 결과 설정
                asyncResultManager.setSuccessResult(
                        event.getTransactionId(),
                        null,
                        "MES가 완료되었습니다.",
                        HttpStatus.OK
                );
                log.info("MES 완료 성공: transactionId={}, mesId={}",
                        event.getTransactionId(), event.getMesId());
            } else {
                // 실패 결과 설정
                asyncResultManager.setErrorResult(
                        event.getTransactionId(),
                        "MES 완료 실패: " + event.getErrorMessage(),
                        HttpStatus.INTERNAL_SERVER_ERROR
                );
                log.error("MES 완료 실패: transactionId={}, mesId={}, error={}",
                        event.getTransactionId(), event.getMesId(), event.getErrorMessage());
            }

            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("MES 완료 완료 이벤트 처리 실패: transactionId={}, mesId={}",
                    event.getTransactionId(), event.getMesId(), e);
            acknowledgment.acknowledge();
        }
    }
}
