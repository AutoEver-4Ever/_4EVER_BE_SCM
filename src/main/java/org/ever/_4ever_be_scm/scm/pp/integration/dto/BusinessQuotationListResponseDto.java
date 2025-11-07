package org.ever._4ever_be_scm.scm.pp.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BusinessQuotationListResponseDto {
    private long total;
    private List<BusinessQuotationDto> content;
    private BusinessPageInfo page;
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusinessPageInfo {
        private int number;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean hasNext;
    }
}
