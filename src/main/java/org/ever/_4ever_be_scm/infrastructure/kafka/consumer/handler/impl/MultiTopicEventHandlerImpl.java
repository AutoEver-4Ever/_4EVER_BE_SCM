package org.ever._4ever_be_scm.infrastructure.kafka.consumer.handler.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ever._4ever_be_scm.infrastructure.kafka.consumer.handler.MultiTopicEventHandler;
import org.ever._4ever_be_scm.infrastructure.kafka.event.*;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MultiTopicEventHandlerImpl implements MultiTopicEventHandler {

    @Override
    public void handleUserEvent(UserEvent event) {
        // User 서비스에서 받은 이벤트 처리
        log.debug("사용자 이벤트 처리 완료 - Action: {}", event.getAction());

        // TODO: 실제 비즈니스 로직 구현
        // 1. 사용자 정보 업데이트
        // 2. 결제 정보와 연동
    }

    @Override
    public void handleScmEvent(ScmEvent event) {
        // SCM 서비스에서 받은 이벤트 처리
        log.debug("SCM 이벤트 처리 완료 - Action: {}", event.getAction());

        // TODO: 실제 비즈니스 로직 구현
        // 1. 재고 정보 확인
        // 2. 주문 상태 업데이트
    }

    @Override
    public void handleBusinessEvent(BusinessEvent event) {
        // Business 서비스에서 받은 이벤트 처리
        log.debug("비즈니스 이벤트 처리 완료 - Action: {}", event.getAction());

        // TODO: 실제 비즈니스 로직 구현
        // 1. 비즈니스 로직 처리
        // 2. 데이터 동기화
    }

    @Override
    public void handleAlarmEvent(AlarmEvent event) {
        // Alarm 서비스에서 받은 이벤트 처리
        log.debug("알림 이벤트 처리 완료 - Type: {}", event.getAlarmType());

        // TODO: 실제 비즈니스 로직 구현
        // 1. 알림 전송 확인
        // 2. 알림 이력 저장
    }
}
