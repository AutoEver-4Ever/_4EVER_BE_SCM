package org.ever.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 견적 업데이트 이벤트 (Business 서버로 전송)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotationUpdateEvent {

    /**
     * 트랜잭션 ID (SAGA 패턴)
     */
    private String transactionId;

    /**
     * 견적 ID
     */
    private String quotationId;

    /**
     * 업데이트할 납기일
     */
    private LocalDate dueDate;

    /**
     * 업데이트할 견적 상태
     */
    private String quotationStatus;
}
