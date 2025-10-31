package org.ever._4ever_be_scm.scm.pp.entity;

import lombok.*;
import jakarta.persistence.*;
import org.ever._4ever_be_scm.common.entity.TimeStamp;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "mrp_run")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class MrpRun extends TimeStamp {
    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "mrp_result_id")
    private String mrpResultId;

    @Column(name = "count")
    private Integer count;

    @Column(name = "quotation_id")
    private String quotationId;

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
