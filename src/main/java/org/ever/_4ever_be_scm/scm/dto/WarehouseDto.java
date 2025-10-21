package org.ever._4ever_be_scm.scm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 창고 정보 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseDto {
    /**
     * 창고 ID (UUID)
     */
    private String warehouseId;
    
    /**
     * 창고 코드
     */
    private String warehouseCode;
    
    /**
     * 창고 이름
     */
    private String warehouseName;
    
    /**
     * 상태
     */
    private String status;
    
    /**
     * 창고 유형
     */
    private String warehouseType;
    
    /**
     * 위치
     */
    private String location;
    
    /**
     * 관리자 이름
     */
    private String manager;
    
    /**
     * 연락처
     */
    private String phone;
}
