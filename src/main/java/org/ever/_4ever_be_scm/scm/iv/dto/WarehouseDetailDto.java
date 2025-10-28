package org.ever._4ever_be_scm.scm.iv.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseDetailDto {
    private WarehouseInfoDto warehouseInfo;
    private ManagerDto manager;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WarehouseInfoDto {
        private String warehouseName;
        private String warehouseNumber;
        private String warehouseType;
        private String statusCode;
        private String location;
        private String description;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ManagerDto {
        private String managerId;
        private String managerName;
        private String managerPhone;
        private String managerEmail;
    }
}
