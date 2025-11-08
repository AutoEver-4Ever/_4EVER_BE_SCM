package org.ever.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 구매 주문 승인 이벤트 (SCM -> Business)
 * SCM에서 구매주문을 승인하면 Business에 PurchaseVoucher 생성 요청
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderApprovalEvent {

    /**
     * 트랜잭션 ID (SAGA 패턴)
     */
    private String transactionId;

    /**
     * 구매 주문 ID (ProductOrder ID)
     */
    private String purchaseOrderId;

    /**
     * 구매 주문 번호
     */
    private String purchaseOrderNumber;

    /**
     * 공급업체 ID
     */
    private String supplierCompanyId;

    /**
     * 총 금액
     */
    private BigDecimal totalAmount;

    /**
     * 지급 기한
     */
    private LocalDateTime dueDate;

    /**
     * 메모
     */
    private String memo;

    /**
     * 이벤트 발행 시각
     */
    private Long timestamp;
}
