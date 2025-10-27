package org.ever._4ever_be_scm.scm.pp.entity;

import lombok.*;
import jakarta.persistence.*;
import org.ever._4ever_be_scm.common.entity.TimeStamp;

import java.util.UUID;

@Entity
@Table(name = "mps_detail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class MpsDetail extends TimeStamp {
    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "mps_id")
    private String mpsId;

    @Column(name = "week_label", length = 50)
    private String weekLabel;

    @Column(name = "demand")
    private Integer demand;

    @Column(name = "required_inventory")
    private Integer requiredInventory;

    @Column(name = "production_needed")
    private Integer productionNeeded;

    @Column(name = "planned_production")
    private Integer plannedProduction;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString().replace("-", "");
        }
    }
}
