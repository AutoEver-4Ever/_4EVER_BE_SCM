package org.ever._4ever_be_scm.infrastructure.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ScmEvent extends BaseEvent {

    private String orderId;
    private String productId;
    private Integer quantity;
    private String warehouseId;
    private ScmAction action;

    public enum ScmAction {
        STOCK_RESERVED,
        STOCK_RELEASED,
        SHIPMENT_CREATED,
        SHIPMENT_COMPLETED
    }
}
