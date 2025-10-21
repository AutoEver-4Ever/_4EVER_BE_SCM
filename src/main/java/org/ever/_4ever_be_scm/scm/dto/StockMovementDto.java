package org.ever._4ever_be_scm.scm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 재고 이동 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMovementDto {
    /**
     * 이동 유형 (입고, 출고, 이동 등)
     */
    private String type;
    
    /**
     * 수량
     */
    private int quantity;
    
    /**
     * 단위
     */
    private String unit;
    
    /**
     * 작업 시간
     */
    private LocalDateTime date;
    
    /**
     * 담당자
     */
    private String manager;

    /**
     * 창고 코드
     */
    private String toWarehouseCode;

    /**
     * 창고 코드
     */
    private String fromWarehouseCode;

    private String referenceCode;

}
