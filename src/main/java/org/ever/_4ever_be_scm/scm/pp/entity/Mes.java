package org.ever._4ever_be_scm.scm.pp.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import lombok.*;
import jakarta.persistence.*;
import org.ever._4ever_be_scm.common.entity.TimeStamp;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * MES (Manufacturing Execution System) - 작업 지시서
 */
@Entity
@Table(name = "mes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class Mes extends TimeStamp {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "mes_number", length = 50)
    private String mesNumber;  // WO-2024-001

    @Column(name = "quotation_id")
    private String quotationId;

    @Column(name = "bom_id")
    private String bomId;

    @Column(name = "product_id")
    private String productId;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "status", length = 20)
    private String status;  // PENDING, IN_PROGRESS, COMPLETED, SKIP

    @Column(name = "current_operation_id")
    private String currentOperationId;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "progress_rate")
    private Integer progressRate;  // 0-100

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = String.valueOf(UuidCreator.getTimeOrderedEpoch());
        }
        if (progressRate == null) {
            progressRate = 0;
        }
        if (status == null) {
            status = "PENDING";
        }
    }
}
