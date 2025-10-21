package org.ever._4ever_be_scm.scm.dto;

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
        private String warehouseCode;
        private String warehouseType;
        private String warehouseStatus;
        private String location;
        private String description;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ManagerDto {
        private String name;
        private String phoneNumber;
        private String email;
    }
}
