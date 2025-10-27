package org.ever._4ever_be_scm.scm.mm.vo;

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
public class PurchaseRequisitionCreateVo {
    private String requesterId;
    private List<ItemVo> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemVo {
        private String itemName;
        private BigDecimal quantity;
        private String uomName;
        private BigDecimal expectedUnitPrice;
        private String preferredSupplierName;
        private LocalDateTime dueDate;
        private String purpose;
        private String note;
    }
}
