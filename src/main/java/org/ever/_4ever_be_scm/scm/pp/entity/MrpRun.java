package org.ever._4ever_be_scm.scm.pp.entity;

import lombok.*;
import jakarta.persistence.*;
import org.ever._4ever_be_scm.common.entity.TimeStamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * MRP 계획 주문 (Purchase Order Plan)
 * MRP 조회에서 선택한 자재들을 실제 주문 계획으로 전환
 */
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

    /**
     * 원자재 제품 ID
     */
    @Column(name = "product_id")
    private String productId;

    /**
     * 주문 수량
     */
    @Column(name = "quantity")
    private BigDecimal quantity;

    /**
     * 참조 견적 ID (여러 견적의 MRP를 합쳐서 주문할 수 있음)
     */
    @Column(name = "quotation_id")
    private String quotationId;

    /**
     * 조달 시작일
     */
    @Column(name = "procurement_start")
    private LocalDate procurementStart;

    /**
     * 예상 도착일
     */
    @Column(name = "expected_arrival")
    private LocalDate expectedArrival;

    /**
     * 참조 MRP ID
     */
    @Column(name = "mrp_id")
    private String mrpId;

    /**
     * 상태: INITIAL(초기), PENDING(구매요청 대기), REQUEST_APPROVED(구매요청 승인됨),
     *      ORDER_APPROVED(발주서 승인됨), DELIVERING(배송중), DELIVERED(입고완료)
     */
    @Column(name = "status", length = 20)
    private String status;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString().replace("-", "");
        }
        if (status == null) {
            status = "PENDING";
        }
    }
}
