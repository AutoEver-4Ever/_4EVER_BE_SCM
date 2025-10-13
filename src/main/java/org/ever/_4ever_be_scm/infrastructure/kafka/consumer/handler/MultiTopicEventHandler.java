package org.ever._4ever_be_scm.infrastructure.kafka.consumer.handler;

import org.ever._4ever_be_scm.infrastructure.kafka.event.*;

/**
 * Multi Topic 이벤트 처리 핸들러 인터페이스
 */
public interface MultiTopicEventHandler {

    /**
     * User 이벤트 처리
     */
    void handleUserEvent(UserEvent event);

    /**
     * SCM 이벤트 처리
     */
    void handleScmEvent(ScmEvent event);

    /**
     * Business 이벤트 처리
     */
    void handleBusinessEvent(BusinessEvent event);

    /**
     * Alarm 이벤트 처리
     */
    void handleAlarmEvent(AlarmEvent event);
}
