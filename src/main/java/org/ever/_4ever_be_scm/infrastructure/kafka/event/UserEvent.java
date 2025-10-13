package org.ever._4ever_be_scm.infrastructure.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserEvent extends BaseEvent {

    private String userId;
    private String userName;
    private String email;
    private UserAction action;

    public enum UserAction {
        CREATED,
        UPDATED,
        DELETED,
        PAYMENT_REQUESTED,
        PAYMENT_COMPLETED
    }
}
