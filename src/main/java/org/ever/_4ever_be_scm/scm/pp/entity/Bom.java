package org.ever._4ever_be_scm.scm.pp.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import lombok.*;
import jakarta.persistence.*;
import org.ever._4ever_be_scm.common.entity.TimeStamp;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "bom")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bom extends TimeStamp {
    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "product_id")
    private String productId;

    @Column(name = "bom_code", length = 50)
    private String bomCode;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "version")
    private Integer version;

    @Column(name = "lead_time")
    private BigDecimal leadTime;

    @Column(name = "selling_price")
    private BigDecimal sellingPrice;

    @Column(name = "origin_price")
    private BigDecimal originPrice;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = String.valueOf(UuidCreator.getTimeOrderedEpoch());  // UUID v7 생성
        }
    }
}
