package org.ever.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * MES 시작 이벤트 (SCM -> Business)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesStartEvent {

    /**
     * 트랜잭션 ID (SAGA 패턴)
     */
    private String transactionId;

    /**
     * MES ID
     */
    private String mesId;

    /**
     * 견적 ID
     */
    private String quotationId;

    /**
     * 이벤트 발행 시각
     */
    private Long timestamp;
}
