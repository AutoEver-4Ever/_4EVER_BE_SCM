package org.ever._4ever_be_scm.scm.iv.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 구매 발주 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderDto {
    /**
     * 구매 발주 ID
     */
    private String purchaseOrderId;

    /**
     * 구매 발주 코드
     */
    private String purchaseOrderCode;

    /**
     * 공급사
     */
    private String supplier;

    /**
     * 주문 일자
     */
    private LocalDateTime orderDate;

    /**
     * 납기 일자
     */
    private LocalDateTime dueDate;

    /**
     * 총 금액
     */
    private BigDecimal totalAmount;

    /**
     * 상태 (입고 대기, 입고 완료)
     */
    private String status;
}
