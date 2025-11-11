package org.ever._4ever_be_scm.scm.pp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuotationGroupListResponseDto {
    
    /**
     * 그룹핑된 견적 목록
     */
    private List<QuotationGroupDto> content;
    
    /**
     * 페이지 정보
     */
    @JsonProperty("page")
    private PageInfo pageInfo;
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuotationGroupDto {
        /**
         * 견적 ID (대표 ID)
         */
        private String quotationId;
        
        /**
         * 견적 번호
         */
        private String quotationNumber;
        
        /**
         * 고객사명
         */
        private String customerName;
        
        /**
         * 요청일
         */
        private String requestDate;
        
        /**
         * 납기일
         */
        private String dueDate;
        
        /**
         * 상태 코드
         */
        private String statusCode;
        
        /**
         * 가용성 상태
         */
        private String availableStatus;
        
        /**
         * 견적 아이템 목록
         */
        private List<QuotationItemDto> items;
    }
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuotationItemDto {
        /**
         * 제품 ID
         */
        private String productId;
        
        /**
         * 제품명
         */
        private String productName;
        
        /**
         * 수량
         */
        private Integer quantity;
        
        /**
         * 단위
         */
        private String uomName;
        
        /**
         * 단가 (Product에서 조회)
         */
        private Integer unitPrice;
    }
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PageInfo {
        private int number;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean hasNext;
    }
}
