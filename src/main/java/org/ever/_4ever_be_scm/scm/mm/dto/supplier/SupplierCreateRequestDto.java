package org.ever._4ever_be_scm.scm.mm.dto.supplier;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SupplierCreateRequestDto {
    private SupplierInfo supplierInfo;
    private ManagerInfo managerInfo;
    private List<MaterialItem> materialList;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class SupplierInfo {
        private String supplierName;
        private String supplierEmail;
        private String supplierPhone;
        private String supplierBaseAddress;
        private String supplierDetailAddress;
        private String category;
        private Integer deliveryLeadTime;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ManagerInfo {
        private String managerName;
        private String managerPhone;
        private String managerEmail;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class MaterialItem {
        private String materialName;
        private String uomCode;
        private Integer unitPrice;
    }
}
