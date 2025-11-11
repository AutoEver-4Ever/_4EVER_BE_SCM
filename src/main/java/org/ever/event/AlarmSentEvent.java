package org.ever.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 알림 발송 이벤트 클래스
 * 알림 서버에서 클라이언트로 알림을 전송하는 경우에 사용한다.
 */
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AlarmSentEvent extends BaseEvent {

    String alarmId;
    String alarmType;            // source와 구분하기 위해 alarm 명명
    String targetId;
    String title;
    String message;
    String linkId;
    String linkType;
}
