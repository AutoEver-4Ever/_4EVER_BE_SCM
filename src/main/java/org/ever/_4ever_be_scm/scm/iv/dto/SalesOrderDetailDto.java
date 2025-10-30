package org.ever._4ever_be_scm.scm.iv.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    private String salesOrderNumber;

    /**
     * 고객사
     */
    private String customerCompanyName;

    /**
     * 납기 일자
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private String dueDate;

    /**
     * 상태
     */
    private String statusCode;

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
        private String uomName;
    }
}
