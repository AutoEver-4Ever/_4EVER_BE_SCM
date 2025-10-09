package org.ever._4ever_be_scm.infrastructure.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessEvent extends BaseEvent {

    private String businessId;
    private String orderId;
    private String userId;
    private BigDecimal totalAmount;
    private BusinessAction action;

    public enum BusinessAction {
        ORDER_CREATED,
        ORDER_CONFIRMED,
        ORDER_CANCELLED,
        INVOICE_GENERATED
    }
}
