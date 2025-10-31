package org.ever._4ever_be_scm.scm.mm.integration.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ever._4ever_be_scm.common.response.ApiResponse;
import org.ever._4ever_be_scm.scm.mm.integration.dto.InternalUserResponseDto;
import org.ever._4ever_be_scm.scm.mm.integration.port.InternalUserServicePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class InternalUserServiceAdapter implements InternalUserServicePort {

    private final RestClient restClient;

    @Value("${external.business.url}")
    private String businessServiceUrl;

    @Override
    public InternalUserResponseDto getInternalUserInfoById(String internalUserId){
        log.info("Business employee 서비스 호출 - InternalUserId: {}", internalUserId);

        try {
            Map<String, String> requestBody = Map.of("InternalUserId", internalUserId);

            ApiResponse<InternalUserResponseDto> response = restClient.post()
                    .uri(businessServiceUrl + "/business/hrm/{userId}/employee", internalUserId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<InternalUserResponseDto>>() {});

            if (response != null && response.isSuccess()) {
                log.info("Business employee 서비스 호출 성공 - InternalUserId: {}", internalUserId);
                return response.getData();
            } else {
                log.error("Business employee 서비스 응답 실패 - response: {}", response);
                throw new RuntimeException("Failed to retrieve internalUser information from business service");
            }
        } catch (Exception e) {
            log.error("Business employee 서비스 호출 중 오류 발생", e);
            throw new RuntimeException("Error calling Business employee service", e);
        }
    }

    @Override
    public List<InternalUserResponseDto>  getInternalUserInfosByIds(List<String> internalUserIds){
        log.info("Business employee 서비스 호출 (multiple) - internalUserIds: {}", internalUserIds);

        try {
            Map<String, List<String>> requestBody = Map.of("userIds", internalUserIds);

            ApiResponse<List<InternalUserResponseDto> > response = restClient.post()
                    .uri(businessServiceUrl + "/business/hrm/employees/multiple")
                    .body(requestBody)
                    .retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<List<InternalUserResponseDto> >>() {});

            if (response != null && response.isSuccess()) {
                log.info("Business employee 서비스 호출 성공 (multiple) - count: {}", internalUserIds.size());
                return response.getData();
            } else {
                log.error("Business employee 서비스 응답 실패 (multiple) - response: {}", response);
                throw new RuntimeException("Failed to retrieve internalUsers from business service");
            }
        } catch (Exception e) {
            log.error("Business employee 서비스 호출 중 오류 발생 (multiple)", e);
            throw new RuntimeException("Error calling Business employee service (multiple)", e);
        }
    }
}
