package org.ever._4ever_be_scm.scm.iv.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    private String itemId;
    private String itemName;
    private String itemNumber;
    private String category;

    // 재고 정보
    private int currentStock;
    private String uomName;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private int safetyStock;
    private String statusCode;

    // 위치 정보
    private String warehouseId;
    private String warehouseName;
    private String warehouseNumber;
    private String location;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastModified;

    // 공급사 이름
    private String supplierCompanyName;

    // 재고 이동 내역
    private List<StockMovementDto> stockMovement;
}
