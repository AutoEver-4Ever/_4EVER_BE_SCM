package org.ever._4ever_be_scm.scm.pp.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import lombok.*;
import jakarta.persistence.*;
import org.ever._4ever_be_scm.common.entity.TimeStamp;

import java.time.LocalDate;

@Entity
@Table(name = "routing")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Routing extends TimeStamp {
    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "bom_item_id")
    private String bomItemId;

    @Column(name = "operation_id")
    private String operationId;

    @Column(name = "sequence")
    private Integer sequence;

    @Column(name = "required_time")
    private Integer requiredTime;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = String.valueOf(UuidCreator.getTimeOrderedEpoch());  // UUID v7 생성
        }
    }
}
