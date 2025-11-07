package org.ever.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 공급사 회사 정보 응답 이벤트
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierCompanyResolveResultEvent {
    private String transactionId;
    private String supplierUserId;
    private String supplierCompanyId;
    private String supplierCompanyName;
    private boolean success;
    private String errorMessage;
    private Long timestamp;
}
