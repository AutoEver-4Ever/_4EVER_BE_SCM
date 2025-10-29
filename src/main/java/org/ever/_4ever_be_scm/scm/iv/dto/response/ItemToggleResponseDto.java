package org.ever._4ever_be_scm.scm.iv.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 자재 품목 토글 목록 응답 DTO
 */
@Getter
@Builder
public class ItemToggleResponseDto {
    
    /**
     * 공급사명
     */
    private String supplierCompanyName;
    
    /**
     * 단위명
     */
    private String uomName;
    
    /**
     * 공급사 ID
     */
    private String supplierCompanyId;
    
    /**
     * 품목명
     */
    private String itemIdName;
    
    /**
     * 품목 ID
     */
    private String itemId;
    
    /**
     * 단가
     */
    private BigDecimal unitPrice;
}
