package org.ever._4ever_be_scm.scm.pp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BomDetailResponseDto {
    private String bomId;
    private String bomNumber;
    private String productId;
    private String productNumber;
    private String productName;
    private String version;
    private String statusCode;
    private LocalDateTime lastModifiedAt;
    private List<BomComponentDto> components;
    private Map<String, List<LevelStructureDto>> levelStructure;
    private List<RoutingDto> routing;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BomComponentDto {
        private String itemId;
        private String code;
        private String name;
        private Integer quantity;
        private String unit;
        private String level;
        private String supplierName;
        private String componentType; // ITEM/PRODUCT
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LevelStructureDto {
        private String code;
        private String name;
        private String quantity;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RoutingDto {
        private Integer sequence;
        private String operationName;
        private String itemName;
        private Integer runTime;
    }
}
