package org.ever.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * MES 완료 이벤트 (SCM -> Business)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesCompleteEvent {

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
     * 생산 수량
     */
    private Integer quantity;

    /**
     * 제품 ID
     */
    private String productId;

    /**
     * 이벤트 발행 시각
     */
    private Long timestamp;
}
