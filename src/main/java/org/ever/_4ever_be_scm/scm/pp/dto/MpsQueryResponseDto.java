package org.ever._4ever_be_scm.scm.pp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MpsQueryResponseDto {
    private String bomId;
    private String productName;
    private List<WeekDto> content;
    private PageInfo page;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WeekDto {
        private String week; // "2025-09-3W"
        private Integer demand;
        private Integer requiredInventory;
        private Integer productionNeeded;
        private Integer plannedProduction;
    }

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
}
