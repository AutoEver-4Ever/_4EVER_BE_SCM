package org.ever._4ever_be_scm.scm.iv.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import lombok.*;
import org.ever._4ever_be_scm.common.entity.TimeStamp;

import jakarta.persistence.*;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "warehouse")
@Getter
public class Warehouse extends TimeStamp {

    /**
     * 창고 고유 식별자 (UUID)
     */
    @Id
    @Column(length = 36)
    private String id;

    /**
     * 내부 사용자 ID (UUID)
     */
    @Column(name = "internal_user_id", length = 36)
    private String internalUserId;

    @Column(name = "warehouse_name", nullable = false)
    private String warehouseName;

    @Column(name = "warehouse_code", nullable = false)
    private String warehouseCode;

    @Column(name = "warehouse_type")
    private String warehouseType;

    @Column(name = "status")
    private String status;

    @Column(name = "location")
    private String location;

    @Column(name = "address")
    private String address;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = String.valueOf(UuidCreator.getTimeOrderedEpoch());  // UUID v7 생성
        }
    }
}
