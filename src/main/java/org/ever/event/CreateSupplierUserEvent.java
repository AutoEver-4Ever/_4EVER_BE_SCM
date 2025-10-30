package org.ever.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSupplierUserEvent {
    private String eventId;
    private String transactionId;

    private String supplierCompanyId;
    private String supplierCompanyCode;
    private String supplierCompanyName;

    private String supplierUserId;
    private String userId;

    private String managerName;
    private String managerEmail;
    private String managerPhone;
}
