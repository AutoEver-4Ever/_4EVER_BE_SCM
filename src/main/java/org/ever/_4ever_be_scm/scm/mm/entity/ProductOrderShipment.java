package org.ever._4ever_be_scm.scm.mm.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import lombok.*;
import jakarta.persistence.*;
import org.ever._4ever_be_scm.common.entity.TimeStamp;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "product_order_shipment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class ProductOrderShipment extends TimeStamp {
    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "delivered_at")
    private LocalDate deliveredAt;

    @Column(name = "expected_delivery")
    private LocalDate expectedDelivery;

    @Column(name = "actual_delivery")
    private LocalDate actualDelivery;

    @Column(name = "status", length = 20)
    private String status;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = String.valueOf(UuidCreator.getTimeOrderedEpoch());  // UUID v7 생성
        }
    }
}
