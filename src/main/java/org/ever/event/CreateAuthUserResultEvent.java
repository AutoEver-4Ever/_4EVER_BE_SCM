package org.ever.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAuthUserResultEvent {
    private String eventId;
    private String transactionId;
    private boolean success;
    private String userId;
    private String failureReason;
}

