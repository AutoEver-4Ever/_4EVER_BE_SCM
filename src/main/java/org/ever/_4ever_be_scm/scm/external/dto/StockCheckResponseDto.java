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
public class StockCheckResponseDto {
    private List<ItemStockDto> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemStockDto {
        private String itemId;
        private String itemName;
        private BigDecimal requiredQuantity;
        private BigDecimal inventoryQuantity;
        private BigDecimal shortageQuantity;
        private String statusCode;
        private Boolean productionRequired;
    }
}
