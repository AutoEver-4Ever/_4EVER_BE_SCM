package org.ever._4ever_be_scm.scm.iv.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 재고 목록 아이템 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryItemDto {
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

    /**
     * 창고타입
     */
    private String warehouseType;

    /**
     * 창고코드
     */
    private String warehouseCode;
    
    /**
     * 재고 수량
     */
    private Integer currentStock;
    
    /**
     * 단위
     */
    private String unit;

    /**
     * 단가
     */
    private BigDecimal price;

    /**
     * 단가
     */
    private BigDecimal totalValue;
    
    /**
     * 안전재고
     */
    private Integer safetyStock;
    
    /**
     * 재고 상태 (정상, 주의, 위험)
     */
    private String status;


}
