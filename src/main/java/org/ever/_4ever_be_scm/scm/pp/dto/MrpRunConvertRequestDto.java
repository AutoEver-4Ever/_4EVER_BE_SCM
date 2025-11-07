package org.ever._4ever_be_scm.scm.pp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * MRP → MRP_RUN 전환 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MrpRunConvertRequestDto {

    /**
     * 선택한 원자재 목록
     */
    private List<MrpItemRequest> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MrpItemRequest {
        private String quotationId;  // 견적 ID (필수!)
        private String itemId;       // productId
        private BigDecimal quantity;
    }
}
