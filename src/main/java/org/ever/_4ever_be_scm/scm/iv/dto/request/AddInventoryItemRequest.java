package org.ever._4ever_be_scm.scm.iv.dto.request;

import lombok.Data;

/**
 * 재고 추가 요청 DTO
 */
@Data
public class AddInventoryItemRequest {
    
    /**
     * 제품 ID
     */
    private String itemId;
    
    /**
     * 안전재고
     */
    private Integer safetyStock;
    
    /**
     * 현재재고
     */
    private Integer currentStock;
    
    /**
     * 창고 ID
     */
    private String warehouseId;
}
