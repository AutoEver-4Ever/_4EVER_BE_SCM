package org.ever._4ever_be_scm.scm.pp.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import lombok.*;
import jakarta.persistence.*;
import org.ever._4ever_be_scm.common.entity.TimeStamp;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "bom_item")
@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BomItem extends TimeStamp {
    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "bom_id")
    private String bomId;

    @Column(name = "component_type", length = 20)
    private String componentType; // MATERIAL(원자재) / ITEM(부품, 완제품)

    public String getComponentType() {
        return componentType == null ? "" : componentType;
    }

    @Column(name = "component_id")
    private String componentId; // 생성 시 항상 하위 BOM의 bomId만 입력 (원자재는 별도 관리)

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "count")
    private BigDecimal count;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = String.valueOf(UuidCreator.getTimeOrderedEpoch());  // UUID v7 생성
        }
    }
}
