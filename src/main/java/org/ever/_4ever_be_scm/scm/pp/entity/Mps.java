package org.ever._4ever_be_scm.scm.pp.entity;

import lombok.*;
import jakarta.persistence.*;
import org.ever._4ever_be_scm.common.entity.TimeStamp;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "mps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class Mps extends TimeStamp {
    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "bom_id")
    private String bomId;

    @Column(name = "quotation_id")
    private String quotationId;

    @Column(name = "mps_code", length = 50)
    private String mpsCode;

    @Column(name = "internal_user_id")
    private String internalUserId;

    @Column(name = "start_week")
    private LocalDate startWeek;

    @Column(name = "end_week")
    private LocalDate endWeek;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString().replace("-", "");
        }
    }
}
