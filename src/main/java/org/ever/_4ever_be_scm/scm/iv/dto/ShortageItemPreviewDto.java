package org.ever._4ever_be_scm.scm.iv.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 재고 부족 간단 정보 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShortageItemPreviewDto {
    /**
     * 제품 ID
     */
    private String itemId;
    
    /**
     * 제품명
     */
    private String itemName;
    
    /**
     * 현재 재고 수량
     */
    private int currentStock;
    
    /**
     * 안전 재고 수량
     */
    private int safetyStock;


    private String uomName;
    
    /**
     * 상태 (주의, 위험)
     */
    private String statusCode;
}
