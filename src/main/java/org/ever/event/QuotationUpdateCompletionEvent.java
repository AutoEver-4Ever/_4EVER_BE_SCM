package org.ever.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 견적 업데이트 완료 이벤트 (Business 서버에서 전송)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotationUpdateCompletionEvent {

    /**
     * 트랜잭션 ID (SAGA 패턴)
     */
    private String transactionId;

    /**
     * 견적 ID
     */
    private String quotationId;

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
