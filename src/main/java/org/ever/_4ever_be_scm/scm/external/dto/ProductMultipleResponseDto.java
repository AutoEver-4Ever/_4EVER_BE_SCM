package org.ever._4ever_be_scm.scm.external.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductMultipleResponseDto {
    private List<ProductDto> products;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductDto {
        private String itemId;
        private String itemNumber;
        private String itemName;
        private String uomName;
        private BigDecimal unitPrice;
    }
}
