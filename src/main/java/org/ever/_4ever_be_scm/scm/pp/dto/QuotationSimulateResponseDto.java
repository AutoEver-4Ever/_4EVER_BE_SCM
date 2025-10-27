package org.ever._4ever_be_scm.scm.pp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuotationSimulateResponseDto {
    private String quotationId;
    private String quotationNumber;
    private String customerCompanyId;
    private String customerCompanyName;
    private String productId;
    private String productName;
    private Integer requestQuantity;
    private LocalDate requestDueDate;
    private SimulationDto simulation;
    private List<ShortageDto> shortages;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SimulationDto {
        private String status; // "SUCCESS" or "FAIL"
        private Integer availableQuantity;
        private Integer shortageQuantity;
        private LocalDate suggestedDueDate;
        private LocalDateTime generatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ShortageDto {
        private String itemId;
        private String itemName;
        private Integer requiredQuantity;
        private Integer currentStock;
        private Integer shortQuantity;
    }
}
