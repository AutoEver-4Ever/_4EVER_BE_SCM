package org.ever._4ever_be_scm.scm.pp.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BusinessQuotationDto {
    private String quotationId;
    private String quotationNumber;
    private String quotationDate;
    private String dueDate;
    private String statusCode;
    private String customerName;
    private String ceoName;
    private List<BusinessQuotationItemDto> items;
    private Long totalAmount;
    
    // 하위 호환성을 위해 기존 필드들도 유지
    private String requestDate; // quotationDate와 동일한 값
    private String availableStatus; // statusCode와 연관
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusinessQuotationItemDto {
        private String itemId;
        private String itemName;
        private Integer quantity;
        private String uomName;
        private Integer unitPrice;
        private Long amount;
    }
}
