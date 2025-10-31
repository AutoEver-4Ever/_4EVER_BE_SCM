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
@Table(name = "product_order")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class ProductOrder extends TimeStamp {
    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "product_order_code", length = 50)
    private String productOrderCode;

    @Column(name = "product_order_type", length = 50)
    private String productOrderType;

    @Column(name = "product_request_id")
    private String productRequestId;

    @Column(name = "requester_id")
    private String requesterId;

    @Column(name = "supplier_company_name")
    private String supplierCompanyName;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approval_id")
    private ProductOrderApproval approvalId;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "etc", length = 255)
    private String etc;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id")
    private ProductOrderShipment shipmentId;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = String.valueOf(UuidCreator.getTimeOrderedEpoch());  // UUID v7 생성
        }
    }
}
