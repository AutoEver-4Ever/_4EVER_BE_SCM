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
        private String itemId;
        private String itemName;
        private Integer requiredQuantity;
        private Integer currentStock;           // 물리적 재고 (availableCount)
        private Integer reservedStock;          // 예약된 재고 (reservedCount) - 추가!
        private Integer actualAvailableStock;   // 실제 사용 가능 재고 (currentStock - reservedStock) - 추가!
        private Integer safetyStock;
        private Integer availableStock;
        private String availableStatusCode; // "SUFFICIENT", "INSUFFICIENT"
        private Integer shortageQuantity;
        private String itemType; // "구매품"
        private LocalDate procurementStartDate;
        private LocalDate expectedArrivalDate;
        private String supplierCompanyName;
    }
}
