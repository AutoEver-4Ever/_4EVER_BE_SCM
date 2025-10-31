package org.ever._4ever_be_scm.scm.pp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MpsPreviewResponseDto {
    private String quotationNumber;
    private String customerCompanyName;
    private String productName;
    private LocalDate confirmedDueDate;
    private List<WeekDto> weeks;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WeekDto {
        private String week; // "2024-02-3W"
        private Integer demand;
        private Integer requiredQuantity;
        private Integer productionQuantity;
        private Integer mps;
    }
}
