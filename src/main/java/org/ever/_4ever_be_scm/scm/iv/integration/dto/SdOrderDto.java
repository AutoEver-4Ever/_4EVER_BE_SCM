package org.ever._4ever_be_scm.scm.iv.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SdOrderDto {
    private String salesOrderId;
    private String salesOrderNumber;
    private String customerName;
    private SdManagerDto manager;
    private String orderDate;
    private String dueDate;
    private Integer totalAmount;
    private String statusCode;
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SdManagerDto {
        private String managerName;
        private String managerPhone;
        private String managerEmail;
    }
}
