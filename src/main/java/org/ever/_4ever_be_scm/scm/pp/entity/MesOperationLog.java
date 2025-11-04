package org.ever._4ever_be_scm.scm.pp.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import lombok.*;
import jakarta.persistence.*;
import org.ever._4ever_be_scm.common.entity.TimeStamp;

import java.time.LocalDateTime;

/**
 * MES 공정별 실행 로그
 */
@Entity
@Table(name = "mes_operation_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class MesOperationLog extends TimeStamp {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "mes_id")
    private String mesId;

    @Column(name = "operation_id")
    private String operationId;

    @Column(name = "sequence")
    private Integer sequence;

    @Column(name = "status", length = 20)
    private String status;  // PENDING, IN_PROGRESS, COMPLETED

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "duration_hours")
    private Double durationHours;

    @Column(name = "manager_id")
    private String managerId;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = String.valueOf(UuidCreator.getTimeOrderedEpoch());
        }
        if (status == null) {
            status = "PENDING";
        }
    }

    /**
     * 공정 시작
     */
    public void start(String managerId) {
        this.status = "IN_PROGRESS";
        this.startedAt = LocalDateTime.now();
        this.managerId = managerId;
    }

    /**
     * 공정 완료
     */
    public void complete() {
        this.status = "COMPLETED";
        this.finishedAt = LocalDateTime.now();

        if (startedAt != null && finishedAt != null) {
            long minutes = java.time.Duration.between(startedAt, finishedAt).toMinutes();
            this.durationHours = minutes / 60.0;
        }
    }
}
