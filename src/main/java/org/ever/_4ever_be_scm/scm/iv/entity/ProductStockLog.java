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
@Table(name = "product_stock_log")
@Getter
public class ProductStockLog extends TimeStamp {

    /**
     * 재고 이력 고유 식별자 (UUID)
     */
    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_stock_id")
    private ProductStock productStock;

    @Column(name = "previous_count")
    private BigDecimal previousCount;

    @Column(name = "change_count")
    private BigDecimal changeCount;

    @Column(name = "current_count")
    private BigDecimal currentCount;

    @Column(name = "movement_type")
    private String movementType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_warehouse_id")
    private Warehouse fromWarehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_warehouse_id")
    private Warehouse toWarehouse;

    @Column(name = "created_by_id")
    private String createdById;

    @Column(name = "reference_code")
    private String referenceCode;

    @Column(name = "note")
    private String note;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = String.valueOf(UuidCreator.getTimeOrderedEpoch());  // UUID v7 생성
        }
    }
}
