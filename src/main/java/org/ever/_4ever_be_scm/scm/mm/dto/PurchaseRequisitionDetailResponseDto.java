package org.ever._4ever_be_scm.scm.mm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class PurchaseRequisitionDetailResponseDto {
    private String id;
    private String purchaseRequisitionNumber;
    private String requesterId;
    private String requesterName;
    private String departmentId;
    private String departmentName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime requestDate;
    private String statusCode;
    private List<ItemDto> items;
    private BigDecimal totalAmount;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemDto {
        private String itemId;
        private String itemName;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime dueDate;
        private BigDecimal quantity;
        private String uomCode;
        private BigDecimal unitPrice;
        private BigDecimal amount;
    }
}
