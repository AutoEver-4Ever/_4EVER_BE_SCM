package org.ever._4ever_be_scm.scm.iv.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 판매 주문 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrderDto {
    /**
     * 판매 주문 ID
     */
    private String salesOrderId;

    /**
     * 판매 주문 코드
     */
    private String salesOrderCode;

    /**
     * 고객사
     */
    private String customer;

    /**
     * 주문 일자
     */
    private String orderDate;

    /**
     * 납기 일자
     */
    private String dueDate;

    /**
     * 총 금액
     */
    private long totalAmount;

    /**
     * 상태 (생산중, 출고 준비완료, 배송중)
     */
    private String status;
}
