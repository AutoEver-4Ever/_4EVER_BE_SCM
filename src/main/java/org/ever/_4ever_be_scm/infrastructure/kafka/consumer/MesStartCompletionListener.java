package org.ever._4ever_be_scm.infrastructure.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ever._4ever_be_scm.common.async.GenericAsyncResultManager;
import org.ever.event.MesStartCompletionEvent;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import static org.ever._4ever_be_scm.infrastructure.kafka.config.KafkaTopicConfig.MES_START_COMPLETION_TOPIC;

/**
 * MES 시작 완료 이벤트 리스너
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MesStartCompletionListener {

    private final GenericAsyncResultManager<Void> asyncResultManager;

    @KafkaListener(topics = MES_START_COMPLETION_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    public void handleMesStartCompletion(MesStartCompletionEvent event, Acknowledgment acknowledgment) {
        log.info("MES 시작 완료 이벤트 수신: transactionId={}, mesId={}, success={}",
                event.getTransactionId(), event.getMesId(), event.isSuccess());

        try {
            if (event.isSuccess()) {
                // 성공 결과 설정
                asyncResultManager.setSuccessResult(
                        event.getTransactionId(),
                        null,
                        "MES가 시작되었습니다.",
                        HttpStatus.OK
                );
                log.info("MES 시작 성공: transactionId={}, mesId={}",
                        event.getTransactionId(), event.getMesId());
            } else {
                // 실패 결과 설정
                asyncResultManager.setErrorResult(
                        event.getTransactionId(),
                        "MES 시작 실패: " + event.getErrorMessage(),
                        HttpStatus.INTERNAL_SERVER_ERROR
                );
                log.error("MES 시작 실패: transactionId={}, mesId={}, error={}",
                        event.getTransactionId(), event.getMesId(), event.getErrorMessage());
            }

            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("MES 시작 완료 이벤트 처리 실패: transactionId={}, mesId={}",
                    event.getTransactionId(), event.getMesId(), e);
            acknowledgment.acknowledge();
        }
    }
}
