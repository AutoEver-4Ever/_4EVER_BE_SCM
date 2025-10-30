package org.ever._4ever_be_scm.scm.iv.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseUpdateRequestDto {
    
    /**
     * 창고명
     */
    private String warehouseName;
    
    /**
     * 창고 타입 (MATERIAL, ITEM, ETC)
     */
    private String warehouseType;
    
    /**
     * 위치/주소
     */
    private String location;
    
    /**
     * 담당자 ID
     */
    private String managerId;
    
    /**
     * 창고 상태 코드 (ACTIVE, INACTIVE)
     */
    private String warehouseStatusCode;
    
    /**
     * 비고
     */
    private String note;
}
