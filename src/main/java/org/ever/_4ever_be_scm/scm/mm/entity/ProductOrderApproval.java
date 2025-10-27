package org.ever._4ever_be_scm.scm.mm.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import lombok.*;
import jakarta.persistence.*;
import org.ever._4ever_be_scm.common.entity.TimeStamp;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "product_order_approval")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class ProductOrderApproval extends TimeStamp {
    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "approval_status", length = 20)
    private String approvalStatus;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDate approvedAt;

    @Column(name = "rejected_reason", length = 255)
    private String rejectedReason;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = String.valueOf(UuidCreator.getTimeOrderedEpoch());  // UUID v7 생성
        }
    }
}
