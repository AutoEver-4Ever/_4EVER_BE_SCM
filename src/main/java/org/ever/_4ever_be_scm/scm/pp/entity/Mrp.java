package org.ever._4ever_be_scm.scm.pp.entity;

import lombok.*;
import jakarta.persistence.*;
import org.ever._4ever_be_scm.common.entity.TimeStamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "mrp")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class Mrp extends TimeStamp {
    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "bom_id")
    private String bomId;

    @Column(name = "quotation_id")
    private String quotationId;

    @Column(name = "product_id")
    private String productId;

    @Column(name = "required_count")
    private BigDecimal requiredCount;

    @Column(name = "procurement_start")
    private LocalDate procurementStart;

    @Column(name = "expected_arrival")
    private LocalDate expectedArrival;

    @Column(name = "status", length = 20)
    private String status;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString().replace("-", "");
        }
    }
}
