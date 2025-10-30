package org.ever._4ever_be_scm.scm.iv.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockTransferRequestDto {
    
    /**
     * 출발 창고 ID
     */
    private String fromWarehouseId;
    
    /**
     * 도착 창고 ID
     */
    private String toWarehouseId;
    
    /**
     * 아이템 ID (Product ID)
     */
    private String itemId;
    
    /**
     * 이동할 재고 수량
     */
    private BigDecimal stockQuantity;
    
    /**
     * 단위명
     */
    private String uomName;

    /**
     * 이동 사유
     */
    private String reason;
}
