package org.ever._4ever_be_scm.scm.iv.integration.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ever._4ever_be_scm.scm.iv.integration.dto.SdApiResponse;
import org.ever._4ever_be_scm.scm.iv.integration.dto.SdOrderDetailResponseDto;
import org.ever._4ever_be_scm.scm.iv.integration.dto.SdOrderListResponseDto;
import org.ever._4ever_be_scm.scm.iv.integration.port.SdOrderServicePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class SdOrderServiceAdapter implements SdOrderServicePort {

    private final RestClient restClient;

    @Value("${external.business.url}")
    private String sdServiceUrl;

    @Override
    public SdOrderListResponseDto getSalesOrderList(int page, int size, String status) {
        log.info("SD 서비스 호출 - 판매 주문 목록 조회: page={}, size={}, status={}", page, size, status);

        try {
            SdApiResponse<SdOrderListResponseDto> response = restClient.get()
                    .uri(sdServiceUrl + "/business/sd/orders?page={page}&size={size}&status={status}", page, size, status)
                    .retrieve()
                    .body(new ParameterizedTypeReference<SdApiResponse<SdOrderListResponseDto>>() {});

            if (response != null && response.isSuccess()) {
                log.info("SD 서비스 호출 성공 - 판매 주문 목록 조회");
                return response.getData();
            } else {
                log.error("SD 서비스 응답 실패 - response: {}", response);
                throw new RuntimeException("Failed to retrieve sales order list from SD service");
            }
        } catch (Exception e) {
            log.error("SD 서비스 호출 중 오류 발생", e);
            throw new RuntimeException("Error calling SD service for sales order list", e);
        }
    }

    @Override
    public SdOrderDetailResponseDto getSalesOrderDetail(String salesOrderId) {
        log.info("SD 서비스 호출 - 판매 주문 상세 조회: salesOrderId={}", salesOrderId);

        try {
            SdApiResponse<SdOrderDetailResponseDto> response = restClient.get()
                    .uri(sdServiceUrl + "/business/sd/orders/{salesOrderId}", salesOrderId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<SdApiResponse<SdOrderDetailResponseDto>>() {});

            if (response != null && response.isSuccess()) {
                log.info("SD 서비스 호출 성공 - 판매 주문 상세 조회");
                return response.getData();
            } else {
                log.error("SD 서비스 응답 실패 - response: {}", response);
                throw new RuntimeException("Failed to retrieve sales order detail from SD service");
            }
        } catch (Exception e) {
            log.error("SD 서비스 호출 중 오류 발생", e);
            throw new RuntimeException("Error calling SD service for sales order detail", e);
        }
    }
}
