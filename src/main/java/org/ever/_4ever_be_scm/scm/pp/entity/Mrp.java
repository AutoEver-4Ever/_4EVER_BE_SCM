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

    /**
     * 부족량 (조달 필요량)
     */
    @Column(name = "shortage_quantity")
    private BigDecimal shortageQuantity;

    /**
     * 이미 소비된 양 (MES 실행 시 증가)
     */
    @Column(name = "consumed_count")
    private BigDecimal consumedCount;

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
