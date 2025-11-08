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
public class MrpQueryResponseDto {
    private PageInfo page;
    private List<MrpItemDto> content;

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
    public static class MrpItemDto {
        private String quotationId;             // 견적 ID (견적별 그룹화)
        private String itemId;
        private String itemName;
        private Integer requiredQuantity;       // MRP 테이블의 requiredCount
        private Integer availableStock;         // INSUFFICIENT: requiredQuantity - shortageQuantity, SUFFICIENT: shortageQuantity
        private String availableStatusCode;     // MRP 테이블의 status ("SUFFICIENT", "INSUFFICIENT")
        private Integer shortageQuantity;       // MRP 테이블의 shortageQuantity
        private Integer consumptionQuantity;    // MRP 테이블의 consumedCount
        private String itemType;                // "구매품"
        private LocalDate procurementStartDate;
        private LocalDate expectedArrivalDate;
        private String supplierCompanyName;
        private String convertStatus;           // MRP Run 전환 상태 ("NOT_CONVERTED", "CONVERTED", "NOT_REQUIRED")
    }
}
