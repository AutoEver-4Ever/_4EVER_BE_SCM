package org.ever._4ever_be_scm.scm.mm.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class StockPurchaseRequestDto {
    private List<Item> items;

    @Data
    public static class Item {
        private String productId;
        private BigDecimal quantity;
    }
}
