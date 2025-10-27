package org.ever._4ever_be_scm.scm.mm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderListResponseDto {
    private String purchaseOrderId;
    private String purchaseOrderNumber;
    private String supplierName;
    private String itemsSummary;
    private LocalDateTime orderDate;
    private LocalDateTime dueDate;
    private BigDecimal totalAmount;
    private String statusCode;
}
