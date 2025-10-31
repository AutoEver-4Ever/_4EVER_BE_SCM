package org.ever._4ever_be_scm.scm.mm.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import lombok.*;
import jakarta.persistence.*;
import org.ever._4ever_be_scm.common.entity.TimeStamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "product_request_item")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class ProductRequestItem extends TimeStamp {
    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "product_request_id")
    private String productRequestId;

    @Column(name = "product_id")
    private String productId;

    @Column(name = "count")
    private BigDecimal count;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "preferred_delivery_date")
    private LocalDateTime preferredDeliveryDate;

    @Column(name = "purpose", length = 255)
    private String purpose;

    @Column(name = "etc", length = 255)
    private String etc;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = String.valueOf(UuidCreator.getTimeOrderedEpoch());  // UUID v7 생성
        }
    }
}
