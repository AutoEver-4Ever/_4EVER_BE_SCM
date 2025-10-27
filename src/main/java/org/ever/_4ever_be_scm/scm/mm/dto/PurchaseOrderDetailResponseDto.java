package org.ever._4ever_be_scm.scm.mm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderDetailResponseDto {
    private String statusCode;
    private LocalDateTime dueDate;
    private String purchaseOrderId;
    private String purchaseOrderNumber;
    private LocalDateTime orderDate;
    private String supplierId;
    private String supplierNumber;
    private String supplierName;
    private String managerPhone;
    private String managerEmail;
    private List<ItemDto> items;
    private BigDecimal totalAmount;
    private String note;

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
