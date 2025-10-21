package org.ever._4ever_be_scm.scm.iv.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 판매 주문 상세 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrderDetailDto {
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
     * 납기 일자
     */
    private String dueDate;

    /**
     * 상태
     */
    private String status;

    /**
     * 주문 항목 목록
     */
    private List<OrderItemDto> orderItems;

    /**
     * 주문 항목 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemDto {
        /**
         * 품목명
         */
        private String itemName;

        /**
         * 수량
         */
        private int quantity;

        /**
         * 단위
         */
        private String unit;
    }
}
