package org.ever.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 공급사 사용자 → 공급사 회사 정보 요청 이벤트
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierCompanyResolveRequestEvent {
    private String transactionId;
    private String supplierUserId;
    private Long timestamp;
}
