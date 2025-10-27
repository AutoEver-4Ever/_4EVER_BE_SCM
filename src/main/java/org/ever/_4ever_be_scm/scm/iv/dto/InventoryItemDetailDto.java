package org.ever._4ever_be_scm.scm.iv.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryItemDetailDto {

    // 제품 정보
    private String productId;
    private String productName;
    private String productCode;
    private String category;

    // 재고 정보
    private int currentStock;
    private String unit;
    private BigDecimal price;
    private BigDecimal totalValue;
    private int safetyStock;
    private String status;

    // 위치 정보
    private String warehouseName;
    private String warehouseCode;
    private String location;
    private LocalDateTime latestLog;

    // 공급사 이름
    private String supplierName;

    // 재고 이동 내역
    private List<StockMovementDto> stockMovement;
}
