package org.ever.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 판매주문 상태 변경 이벤트 (SCM -> Business)
 * SCM에서 재고를 먼저 차감한 후 Business로 상태 변경을 요청
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderStatusChangeEvent {

    /**
     * 트랜잭션 ID (SAGA 패턴)
     */
    private String transactionId;

    /**
     * 판매 주문 ID
     */
    private String salesOrderId;

    /**
     * 이벤트 발행 시각
     */
    private Long timestamp;
}
