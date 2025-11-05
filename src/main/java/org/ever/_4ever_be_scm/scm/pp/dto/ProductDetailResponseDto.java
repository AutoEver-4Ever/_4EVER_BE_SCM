package org.ever._4ever_be_scm.scm.pp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Product 상세 정보 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDetailResponseDto {
    /**
     * Product ID
     */
    private String productId;

    /**
     * Product 이름
     */
    private String productName;

    /**
     * Product 타입 (카테고리)
     */
    private String category;

    /**
     * 제품 코드
     */
    private String productNumber;

    /**
     * 단위
     */
    private String uomName;

    /**
     * 단가 (원가)
     */
    private BigDecimal unitPrice;

    /**
     * 공급업체명
     */
    private String supplierName;
}
