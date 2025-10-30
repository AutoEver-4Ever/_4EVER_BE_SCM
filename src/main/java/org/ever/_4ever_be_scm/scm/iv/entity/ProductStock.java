package org.ever._4ever_be_scm.scm.iv.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import lombok.*;
import org.ever._4ever_be_scm.common.entity.TimeStamp;

import jakarta.persistence.*;
import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "product_stock")
@Getter
public class ProductStock extends TimeStamp {

    /**
     * 재고 고유 식별자 (UUID)
     */
    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "status")
    private String status;

    @Column(name = "available_count")
    private BigDecimal availableCount;

    @Column(name = "safety_count")
    private BigDecimal safetyCount;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = String.valueOf(UuidCreator.getTimeOrderedEpoch());  // UUID v7 생성
        }
    }

    public void setSafetyCount(BigDecimal safetyCount) {
        this.safetyCount = safetyCount;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
