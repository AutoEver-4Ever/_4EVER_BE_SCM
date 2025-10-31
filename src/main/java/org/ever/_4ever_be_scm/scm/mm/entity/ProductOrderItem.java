package org.ever._4ever_be_scm.scm.mm.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import lombok.*;
import jakarta.persistence.*;
import org.ever._4ever_be_scm.common.entity.TimeStamp;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "product_order_item")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class ProductOrderItem extends TimeStamp {
    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "product_order_id")
    private String productOrderId;

    @Column(name = "product_id")
    private String productId;

    @Column(name = "count")
    private BigDecimal count;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "price")
    private BigDecimal price;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = String.valueOf(UuidCreator.getTimeOrderedEpoch());  // UUID v7 생성
        }
    }
}
