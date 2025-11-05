package org.ever.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 판매주문 상태 변경 이벤트 (SCM -> Business)
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
     * 아이템 ID 리스트 (출하할 제품들)
     */
    private List<String> itemIds;

    /**
     * 이벤트 발행 시각
     */
    private Long timestamp;
}
