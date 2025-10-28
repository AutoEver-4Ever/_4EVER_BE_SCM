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
public class StockDeliveryRequestDto {
    
    /**
     * 아이템 ID (Product ID)
     */
    private String itemId;
    
    /**
     * 입출고 수량
     */
    private BigDecimal quantity;
    
    /**
     * 참조 코드
     */
    private String referenceCode;
    
    /**
     * 입출고 사유
     */
    private String reason;
}
