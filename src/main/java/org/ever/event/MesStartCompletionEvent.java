package org.ever.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * MES 시작 완료 이벤트 (Business -> SCM)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesStartCompletionEvent {

    /**
     * 트랜잭션 ID (SAGA 패턴)
     */
    private String transactionId;

    /**
     * MES ID
     */
    private String mesId;

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
