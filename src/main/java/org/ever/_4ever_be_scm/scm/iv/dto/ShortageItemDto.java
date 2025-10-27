package org.ever._4ever_be_scm.scm.iv.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 재고 부족 아이템 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShortageItemDto {
    /**
     * 제품 ID
     */
    private String productId;
    
    /**
     * 제품 코드
     */
    private String productCode;
    
    /**
     * 제품명
     */
    private String productName;
    
    /**
     * 카테고리
     */
    private String category;
    
    /**
     * 창고명
     */
    private String warehouseName;

    private String warehouseCode;
    
    /**
     * 현재 재고 수량
     */
    private int currentStock;
    
    /**
     * 안전 재고 수량
     */
    private int safetyStock;
    
    /**
     * 단위
     */
    private String unit;
    
    /**
     * 부족 수량
     */
    private int shortageAmount;

    private BigDecimal price;

    private BigDecimal totalValue;
    
    /**
     * 상태 (주의, 위험)
     */
    private String status;
}
