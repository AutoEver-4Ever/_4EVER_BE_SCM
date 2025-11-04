package org.ever._4ever_be_scm.scm.pp.integration.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ever._4ever_be_scm.scm.pp.integration.dto.BusinessApiResponse;
import org.ever._4ever_be_scm.scm.pp.integration.dto.BusinessQuotationDto;
import org.ever._4ever_be_scm.scm.pp.integration.dto.BusinessQuotationListResponseDto;
import org.ever._4ever_be_scm.scm.pp.integration.port.BusinessQuotationServicePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
@Slf4j
public class BusinessQuotationServiceAdapter implements BusinessQuotationServicePort {

    private final RestClient restClient;

    @Value("${external.business.url}")
    private String businessServiceUrl;

    @Override
    public BusinessQuotationListResponseDto getQuotationList(
            String statusCode, 
            LocalDate startDate, 
            LocalDate endDate, 
            int page, 
            int size) {
        
        log.info("Business 서비스 호출 - 견적 목록 조회: statusCode={}, startDate={}, endDate={}, page={}, size={}", 
                statusCode, startDate, endDate, page, size);

        try {
            // URI 빌더를 사용하여 쿼리 파라미터 구성
            UriComponentsBuilder uriBuilder = UriComponentsBuilder
                    .fromUriString(businessServiceUrl + "/business/sd/scm/quotations")
                    .queryParam("statusCode", statusCode != null ? statusCode : "ALL")
                    .queryParam("page", page)
                    .queryParam("size", size);
            
            // 날짜 파라미터 추가 (null이 아닌 경우만)
            if (startDate != null) {
                uriBuilder.queryParam("startDate", startDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
            }
            if (endDate != null) {
                uriBuilder.queryParam("endDate", endDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
            }
            
            String uri = uriBuilder.toUriString();
            log.debug("호출 URI: {}", uri);

            BusinessApiResponse<BusinessQuotationListResponseDto> response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(new ParameterizedTypeReference<BusinessApiResponse<BusinessQuotationListResponseDto>>() {});

            if (response != null && response.isSuccess()) {
                log.info("Business 서비스 호출 성공 - 견적 목록 조회: 총 {}건", response.getData().getTotal());
                return response.getData();
            } else {
                log.error("Business 서비스 응답 실패 - response: {}", response);
                throw new RuntimeException("Failed to retrieve quotation list from Business service");
            }
        } catch (Exception e) {
            log.error("Business 서비스 호출 중 오류 발생", e);
            throw new RuntimeException("Error calling Business service for quotation list", e);
        }
    }

    @Override
    public BusinessQuotationDto getQuotationById(String quotationId) {
        log.info("Business 서비스 호출 - 견적 상세 조회: quotationId={}", quotationId);

        try {
            String uri = businessServiceUrl + "/business/sd/quotations/" + quotationId;
            log.debug("호출 URI: {}", uri);

            BusinessApiResponse<BusinessQuotationDto> response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(new ParameterizedTypeReference<BusinessApiResponse<BusinessQuotationDto>>() {});

            if (response != null && response.isSuccess()) {
                log.info("Business 서비스 호출 성공 - 견적 상세 조회: quotationId={}", quotationId);
                return response.getData();
            } else {
                log.error("Business 서비스 응답 실패 - response: {}", response);
                throw new RuntimeException("Failed to retrieve quotation from Business service");
            }
        } catch (Exception e) {
            log.error("Business 서비스 호출 중 오류 발생", e);
            throw new RuntimeException("Error calling Business service for quotation detail", e);
        }
    }
}
