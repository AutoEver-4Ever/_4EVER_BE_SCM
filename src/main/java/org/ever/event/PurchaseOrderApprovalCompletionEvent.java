package org.ever.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 구매 주문 승인 완료 이벤트 (Business -> SCM)
 * Business에서 PurchaseVoucher 생성 완료 후 SCM에 결과 통보
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderApprovalCompletionEvent {

    /**
     * 트랜잭션 ID (SAGA 패턴)
     */
    private String transactionId;

    /**
     * 구매 주문 ID (ProductOrder ID)
     */
    private String purchaseOrderId;

    /**
     * 성공 여부
     */
    private boolean success;

    /**
     * 에러 메시지 (실패 시)
     */
    private String errorMessage;

    /**
     * 이벤트 발행 시각
     */
    private Long timestamp;
}
