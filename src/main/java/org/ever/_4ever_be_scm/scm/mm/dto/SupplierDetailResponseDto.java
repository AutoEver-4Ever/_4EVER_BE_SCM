package org.ever._4ever_be_scm.scm.mm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierDetailResponseDto {
    private SupplierInfoDto supplierInfo;
    private ManagerInfoDto managerInfo;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SupplierInfoDto {
        private String supplierId;
        private String supplierName;
        private String supplierNumber;
        private String businessNumber;
        private String supplierEmail;
        private String supplierPhone;
        private String supplierBaseAddress;
        private String supplierDetailAddress;
        private String supplierStatusCode;
        private String category;
        private Integer deliveryLeadTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ManagerInfoDto {
        private String managerName;
        private String managerPhone;
        private String managerEmail;
    }
}
