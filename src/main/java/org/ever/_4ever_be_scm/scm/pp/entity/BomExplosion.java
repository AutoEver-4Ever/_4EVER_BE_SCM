package org.ever._4ever_be_scm.scm.pp.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import lombok.*;
import jakarta.persistence.*;
import org.ever._4ever_be_scm.common.entity.TimeStamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bom_explosion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BomExplosion extends TimeStamp {
    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "parent_bom_id")
    private String parentBomId;

    @Column(name = "component_product_id")
    private String componentProductId;

    @Column(name = "level")
    private Integer level;

    @Column(name = "total_required_count")
    private BigDecimal totalRequiredCount;

    @Column(name = "path", length = 255)
    private String path;

    @Column(name = "routing_id")
    private String routingId;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = String.valueOf(UuidCreator.getTimeOrderedEpoch());  // UUID v7 생성
        }
    }
}
