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
public class MesQueryResponseDto {
    private Integer size;
    private Integer totalPages;
    private Integer page;
    private Integer totalElements;
    private List<MesItemDto> content;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MesItemDto {
        private String mesId;
        private String mesNumber;
        private String productId;
        private String productName;
        private Integer quantity;
        private String uomName;
        private String quotationId;
        private String quotationNumber;
        private String status;
        private String currentOperation;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer progressRate;
        private List<String> sequence;
    }
}
