package org.ever._4ever_be_scm.scm.iv.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    private String salesOrderNumber;

    /**
     * 고객사
     */
    private String customerName;

    /**
     * 주문 일자
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private String orderDate;

    /**
     * 납기 일자
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private String dueDate;

    /**
     * 총 금액
     */
    private long totalAmount;

    /**
     * 상태 (생산중, 출고 준비완료, 배송중)
     */
    private String statusCode;
}
