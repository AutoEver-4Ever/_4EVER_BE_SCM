package org.ever._4ever_be_scm.scm.iv.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SdOrderListResponseDto {
    private List<SdOrderDto> content;
    private SdPageInfo page;
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SdPageInfo {
        private int number;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean hasNext;
    }
}
