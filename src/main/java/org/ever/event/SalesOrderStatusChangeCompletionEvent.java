package org.ever.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 판매주문 상태 변경 완료 이벤트 (Business -> SCM)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderStatusChangeCompletionEvent {

    /**
     * 트랜잭션 ID (SAGA 패턴)
     */
    private String transactionId;

    /**
     * 판매 주문 ID
     */
    private String salesOrderId;

    /**
     * 아이템 ID 리스트
     */
    private List<String> itemIds;

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
