package org.ever._4ever_be_scm.scm.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 재고 아이템 필터링을 위한 VO 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryFilterVo {
    /**
     * 제품 카테고리
     */
    private String category;
    
    /**
     * 재고 상태
     */
    private String status;
    
    /**
     * 창고 ID
     */
    private String warehouseId;
    
    /**
     * 제품명 검색
     */
    private String itemName;
}
