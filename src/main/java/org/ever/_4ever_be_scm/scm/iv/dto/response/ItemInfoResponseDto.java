package org.ever._4ever_be_scm.scm.iv.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "제품 정보 응답")
public class ItemInfoResponseDto {
    
    private String itemId;
    
    private String itemName;
    
    private String itemNumber;
    
    private BigDecimal unitPrice;
    
    private String supplierName;
}
