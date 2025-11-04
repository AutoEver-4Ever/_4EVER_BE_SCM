package org.ever._4ever_be_scm.scm.pp.integration.port;

import org.ever._4ever_be_scm.scm.pp.integration.dto.BusinessQuotationDto;
import org.ever._4ever_be_scm.scm.pp.integration.dto.BusinessQuotationListResponseDto;

import java.time.LocalDate;

public interface BusinessQuotationServicePort {
    
    /**
     * Business 서비스에서 견적 목록을 조회합니다.
     * 
     * @param statusCode 견적 상태 (ALL, REVIEW, APPROVAL)
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 견적 목록
     */
    BusinessQuotationListResponseDto getQuotationList(
            String statusCode, 
            LocalDate startDate, 
            LocalDate endDate, 
            int page, 
            int size
    );
    
    /**
     * Business 서비스에서 특정 견적을 조회합니다.
     * 
     * @param quotationId 견적 ID
     * @return 견적 정보
     */
    BusinessQuotationDto getQuotationById(String quotationId);
}
