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
public class BomCreateRequestDto {
    private String productName;
    private String unit;
    private List<BomItemRequestDto> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BomItemRequestDto {
    private String itemId; // productId 또는 하위 bomId
    private Integer quantity;
    private String operationId;
    private Integer sequence;
    // componentType은 서비스에서 itemId로 자동 판별
    }
}
