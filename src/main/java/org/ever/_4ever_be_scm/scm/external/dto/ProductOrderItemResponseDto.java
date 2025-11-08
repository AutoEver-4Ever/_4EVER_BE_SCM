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
public class ProductOrderItemResponseDto {
    private List<ItemDto> items;
    private BigDecimal totalPrice;
    private String productOrderNumber;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemDto {
        private String itemId;
        private String itemName;
        private BigDecimal quantity;
        private String uomName;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
    }
}
