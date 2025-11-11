package org.ever.event;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.ever._4ever_be_scm.infrastructure.kafka.event.BaseEvent;
import org.ever.event.alarm.AlarmType;
import org.ever.event.alarm.LinkType;
import org.ever.event.alarm.TargetType;

/**
 * 알림 이벤트 클래스
 * 알림 외 서버에서 알림 서버에 알림을 요청하는 경우에 사용한다.
 */
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AlarmEvent extends BaseEvent {

    String alarmId;
    AlarmType alarmType; // source와 구분하기 위해 alarm 명명
    String targetId;
    TargetType targetType;
    String title;
    String message;
    String linkId;
    LinkType linkType;
    LocalDateTime scheduledAt; // 예약 발송 시간(아직 미구현) -> null 이면 즉시 발송
}
