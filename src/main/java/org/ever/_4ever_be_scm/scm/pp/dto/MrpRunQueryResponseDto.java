package org.ever._4ever_be_scm.scm.pp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MrpRunQueryResponseDto {
    private PageInfo page;
    private List<MrpRunItemDto> content;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PageInfo {
        private Integer number;
        private Integer size;
        private Integer totalElements;
        private Integer totalPages;
        private Boolean hasNext;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MrpRunItemDto {
        private String mrpRunId;
        private String itemId;
        private String quotationNumber;
        private String itemName;
        private BigDecimal quantity;
        private String status;  // PENDING, APPROVAL, REJECTED, COMPLETED
        private LocalDate procurementStartDate;
        private LocalDate expectedArrivalDate;
    }
}
