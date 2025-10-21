package org.ever._4ever_be_scm.scm.dto;

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
    private String productId;
    
    /**
     * 제품명
     */
    private String productName;
    
    /**
     * 현재 재고 수량
     */
    private int stockQuantity;
    
    /**
     * 안전 재고 수량
     */
    private int safetyStock;
    
    /**
     * 부족 수량
     */
    private int shortageAmount;

    private String unit;
    
    /**
     * 상태 (주의, 위험)
     */
    private String status;
}
