package org.ever.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class StatusEvent extends BaseEvent {

    private String correlationId;       // 원본 이벤트 고유 ID - ex)AlarmEvent의 eventId
    private String entityId;            // 상태가 적용되는 엔티티 ID - ex) Notification(Entity) ID 등
    private EventStatusEnum status;
    private FailureInfo failureDetails; // 실패 세부 정보 - 재시도 횟수, 오류 메시지 등

    public enum EventStatusEnum {
        SUCCESS,
        FAILURE,
        RETRYING,
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FailureInfo {

        private String errorCode;
        private String message;
        private Integer retryCount;
    }
}
