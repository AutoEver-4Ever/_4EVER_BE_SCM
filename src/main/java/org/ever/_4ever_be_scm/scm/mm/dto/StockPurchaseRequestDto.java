package org.ever._4ever_be_scm.scm.mm.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class StockPurchaseRequestDto {
    private String requesterId; // 요청자 ID (optional, 시스템에서 지정 가능)
    private List<Item> items;

    @Data
    public static class Item {
        private String productId;
        private BigDecimal quantity;
    }
}
