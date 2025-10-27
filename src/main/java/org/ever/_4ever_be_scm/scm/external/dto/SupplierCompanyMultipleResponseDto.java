package org.ever._4ever_be_scm.scm.external.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierCompanyMultipleResponseDto {
    private List<SupplierCompanyDto> supplierCompanies;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SupplierCompanyDto {
        private String companyId;
        private String companyNumber;
        private String companyName;
        private String baseAddress;
        private String detailAddress;
        private String category;
        private String officePhone;
        private String managerId;
    }
}
