package org.ever._4ever_be_scm.scm.pp.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import lombok.*;
import jakarta.persistence.*;
import org.ever._4ever_be_scm.common.entity.TimeStamp;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "operation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Operation extends TimeStamp {
    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "op_code", length = 20)
    private String opCode;

    @Column(name = "op_name", length = 50)
    private String opName;

    @Column(name = "description")
    private String description;

    @Column(name = "required_time")
    private BigDecimal requiredTime;

}
