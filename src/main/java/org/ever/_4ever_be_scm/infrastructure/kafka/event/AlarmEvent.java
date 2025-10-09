package org.ever._4ever_be_scm.infrastructure.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AlarmEvent extends BaseEvent {

    private String userId;
    private String title;
    private String message;
    private AlarmType alarmType;
    private AlarmPriority priority;

    public enum AlarmType {
        PAYMENT_SUCCESS,
        PAYMENT_FAILED,
        ORDER_CONFIRMED,
        SHIPMENT_NOTIFICATION,
        GENERAL_NOTIFICATION
    }

    public enum AlarmPriority {
        LOW,
        MEDIUM,
        HIGH,
        URGENT
    }
}
