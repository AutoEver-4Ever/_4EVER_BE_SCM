package org.ever._4ever_be_scm.scm.mm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierProfileResponseDto {
    
    // SupplierUser 정보
    private String supplierUserName;
    private String supplierUserEmail;
    private String supplierUserPhoneNumber;
    
    // SupplierCompany 정보
    private String companyName;
    private String businessNumber;
    private String baseAddress;
    private String detailAddress;
    private String officePhone;
}
