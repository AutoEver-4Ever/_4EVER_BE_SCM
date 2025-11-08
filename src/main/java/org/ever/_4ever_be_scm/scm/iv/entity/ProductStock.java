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
    
    @Column(name = "reserved_count")
    @Builder.Default
    private BigDecimal reservedCount = BigDecimal.ZERO;


    @Column(name = "for_shipment_count")
    @Builder.Default
    private BigDecimal forShipmentCount = BigDecimal.ZERO;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = String.valueOf(UuidCreator.getTimeOrderedEpoch());  // UUID v7 생성
        }
    }

    public void setSafetyCount(BigDecimal safetyCount) {
        this.safetyCount = safetyCount;
    }

    public void setAvailableCount(BigDecimal availableCount) {
        this.availableCount = availableCount;
    }

    public void setForShipmentCount(BigDecimal forShipmentCount) {
        this.forShipmentCount = forShipmentCount;
    }
    
    /**
     * 실제 사용 가능한 재고량 (가용재고 - 예약재고)
     */
    public BigDecimal getActualAvailableCount() {
        BigDecimal available = availableCount != null ? availableCount : BigDecimal.ZERO;
        BigDecimal reserved = reservedCount != null ? reservedCount : BigDecimal.ZERO;
        return available.subtract(reserved);
    }
    
    /**
     * 재고 예약
     */
    public boolean reserveStock(BigDecimal quantity) {
        if (getActualAvailableCount().compareTo(quantity) >= 0) {
            this.reservedCount = (reservedCount != null ? reservedCount : BigDecimal.ZERO).add(quantity);
            return true;
        }
        return false;
    }
    
    /**
     * 재고 예약 해제
     */
    public void releaseReservation(BigDecimal quantity) {
        this.reservedCount = (reservedCount != null ? reservedCount : BigDecimal.ZERO).subtract(quantity);
        if (this.reservedCount.compareTo(BigDecimal.ZERO) < 0) {
            this.reservedCount = BigDecimal.ZERO;
        }
    }
    
    /**
     * 예약된 재고를 실제로 차감
     */
    public void consumeReservedStock(BigDecimal quantity) {
        BigDecimal currentReserved = reservedCount != null ? reservedCount : BigDecimal.ZERO;
        BigDecimal currentAvailable = availableCount != null ? availableCount : BigDecimal.ZERO;

        // 1. 예약된 것 중 실제 사용할 만큼만 해제 (min(요청량, 예약량))
        BigDecimal reservedToRelease = currentReserved.min(quantity);
        releaseReservation(reservedToRelease);

        // 2. 실제재고는 전체 요청량 차감
        this.availableCount = currentAvailable.subtract(quantity);
        if (this.availableCount.compareTo(BigDecimal.ZERO) < 0) {
            this.availableCount = BigDecimal.ZERO;
        }
    }

    /**
     * 예약된 재고를 실제로 차감
     */
    public void consumeReservedForShipmentStock(BigDecimal quantity) {
        BigDecimal currentReserved = reservedCount != null ? reservedCount : BigDecimal.ZERO;
        BigDecimal currentForShipmentCount = forShipmentCount != null ? forShipmentCount : BigDecimal.ZERO;

        // 1. 예약된 것 중 실제 사용할 만큼만 해제 (min(요청량, 예약량))
        BigDecimal reservedToRelease = currentReserved.min(quantity);
        releaseReservation(reservedToRelease);

        // 2. 실제재고는 전체 요청량 차감
        this.forShipmentCount = currentForShipmentCount.subtract(quantity);
        if (this.forShipmentCount.compareTo(BigDecimal.ZERO) < 0) {
            this.forShipmentCount = BigDecimal.ZERO;
        }
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
