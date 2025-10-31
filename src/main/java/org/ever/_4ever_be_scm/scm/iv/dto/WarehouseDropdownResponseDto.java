package org.ever._4ever_be_scm.scm.iv.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseDropdownResponseDto {
    
    private List<WarehouseDropdownItem> warehouses;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WarehouseDropdownItem {
        
        /**
         * 창고 번호 (warehouseCode)
         */
        private String warehouseNumber;
        
        /**
         * 창고 ID
         */
        private String warehouseId;
        
        /**
         * 창고명
         */
        private String warehouseName;
    }
}
