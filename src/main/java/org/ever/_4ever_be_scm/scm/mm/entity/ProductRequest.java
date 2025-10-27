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
@Table(name = "product_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class ProductRequest extends TimeStamp {
    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "product_request_code", length = 50)
    private String productRequestCode;

    @Column(name = "product_request_type", length = 50)
    private String  productRequestType;

    @Column(name = "requester_id")
    private String requesterId;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approval_id")
    private ProductRequestApproval approvalId;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = String.valueOf(UuidCreator.getTimeOrderedEpoch());  // UUID v7 생성
        }
    }
}
