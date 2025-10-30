package org.ever._4ever_be_scm.scm.mm.service.impl;

import com.github.f4b6a3.uuid.UuidCreator;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ever._4ever_be_scm.common.async.AsyncResultManager;
import org.ever._4ever_be_scm.common.response.ApiResponse;
import org.ever._4ever_be_scm.common.saga.SagaTransactionManager;
import org.ever._4ever_be_scm.scm.iv.entity.SupplierCompany;
import org.ever._4ever_be_scm.scm.iv.entity.SupplierUser;
import org.ever._4ever_be_scm.scm.iv.repository.SupplierCompanyRepository;
import org.ever._4ever_be_scm.scm.iv.repository.SupplierUserRepository;
import org.ever._4ever_be_scm.scm.mm.dto.SupplierDetailResponseDto;
import org.ever._4ever_be_scm.scm.mm.dto.SupplierListResponseDto;
import org.ever._4ever_be_scm.scm.mm.dto.supplier.SupplierCreateRequestDto;
import org.ever._4ever_be_scm.scm.mm.integration.port.SupplierUserServicePort;
import org.ever._4ever_be_scm.scm.mm.service.SupplierService;
import org.ever._4ever_be_scm.scm.mm.service.model.SupplierCreationResult;
import org.ever._4ever_be_scm.scm.mm.vo.SupplierSearchVo;
import org.ever.event.CreateAuthUserResultEvent;
import org.ever.event.CreateSupplierUserEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.async.DeferredResult;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierCompanyRepository supplierCompanyRepository;
    private final SupplierUserRepository supplierUserRepository;
    private final AsyncResultManager<CreateAuthUserResultEvent> asyncResultManager;
    private final SagaTransactionManager sagaTransactionManager;
    private final SupplierUserServicePort supplierUserServicePort;

    @Override
    @Transactional(readOnly = true)
    public Page<SupplierListResponseDto> getSupplierList(SupplierSearchVo searchVo) {
        PageRequest pageRequest = PageRequest.of(searchVo.getPage(), searchVo.getSize());

        Page<SupplierCompany> supplierCompanies = supplierCompanyRepository.findAll(pageRequest);

        List<SupplierListResponseDto> dtoList = new ArrayList<>();
        for (SupplierCompany supplierCompany : supplierCompanies.getContent()) {
            String category = supplierCompany.getCategory();
            String status = supplierCompany.getStatus();

            if (status == null || category == null) {
                continue;
            }

            if (!"ALL".equals(searchVo.getStatusCode()) && !status.equals(searchVo.getStatusCode())) {
                continue;
            }
            if (!"ALL".equals(searchVo.getCategory()) && !category.equals(searchVo.getCategory())) {
                continue;
            }

            dtoList.add(SupplierListResponseDto.builder()
                .statusCode(status)
                .supplierInfo(SupplierListResponseDto.SupplierInfoDto.builder()
                    .supplierId(supplierCompany.getId())
                    .supplierName(supplierCompany.getCompanyName())
                    .supplierNumber(supplierCompany.getCompanyCode())
                    .supplierEmail(supplierCompany.getSupplierUser() != null ? supplierCompany.getSupplierUser().getSupplierUserEmail() : null)
                    .supplierPhone(supplierCompany.getOfficePhone())
                    .supplierBaseAddress(supplierCompany.getBaseAddress())
                    .supplierDetailAddress(supplierCompany.getDetailAddress())
                    .supplierStatusCode(status)
                    .category(category)
                    .deliveryLeadTime(supplierCompany.getDeliveryDays())
                    .build())
                .build());
        }

        return new PageImpl<>(dtoList, pageRequest, supplierCompanies.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierDetailResponseDto getSupplierDetail(String supplierId) {
        SupplierCompany supplierCompany = supplierCompanyRepository.findById(supplierId)
            .orElseThrow(() -> new IllegalArgumentException("공급업체를 찾을 수 없습니다."));

        String category = supplierCompany.getCategory();
        String status = supplierCompany.getStatus();
        SupplierUser supplierUser = supplierCompany.getSupplierUser();

        return SupplierDetailResponseDto.builder()
            .statusCode(status)
            .supplierInfo(SupplierDetailResponseDto.SupplierInfoDto.builder()
                .supplierId(supplierCompany.getId())
                .supplierName(supplierCompany.getCompanyName())
                .supplierNumber(supplierCompany.getCompanyCode())
                .supplierEmail(supplierUser != null ? supplierUser.getSupplierUserEmail() : null)
                .supplierPhone(supplierCompany.getOfficePhone())
                .supplierBaseAddress(supplierCompany.getBaseAddress())
                .supplierDetailAddress(supplierCompany.getDetailAddress())
                .supplierStatus(status)
                .category(category)
                .deliveryLeadTime(supplierCompany.getDeliveryDays())
                .build())
            .managerInfo(SupplierDetailResponseDto.ManagerInfoDto.builder()
                .managerName(supplierUser != null ? supplierUser.getSupplierUserName() : null)
                .managerPhone(supplierUser != null ? supplierUser.getSupplierUserPhoneNumber() : supplierCompany.getOfficePhone())
                .managerEmail(supplierUser != null ? supplierUser.getSupplierUserEmail() : null)
                .build())
            .build();
    }

    @Override
    @Transactional
    public void createSupplier(
        SupplierCreateRequestDto dto,
        DeferredResult<ResponseEntity<ApiResponse<CreateAuthUserResultEvent>>> deferredResult
    ) {
        log.info("[SAGA][SUPPLIER] 공급사 등록 시작 - supplierName: {}", dto.getSupplierInfo() != null ? dto.getSupplierInfo().getSupplierName() : null);

        if (dto.getSupplierInfo() == null || dto.getManagerInfo() == null) {
            deferredResult.setResult(
                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail("공급사 정보와 담당자 정보는 필수입니다.", HttpStatus.BAD_REQUEST))
            );
            return;
        }

        String transactionId = String.valueOf(UuidCreator.getTimeOrderedEpoch());
        asyncResultManager.registerResult(transactionId, deferredResult);

        sagaTransactionManager.executeSagaWithId(transactionId, () -> {
            try {
                String externalUserId = String.valueOf(UuidCreator.getTimeOrderedEpoch());

                SupplierCreationResult creationResult = saveSupplier(dto, externalUserId);
                SupplierCompany supplierCompany = creationResult.supplierCompany();
                SupplierUser supplierUser = creationResult.supplierUser();

                CreateSupplierUserEvent event = CreateSupplierUserEvent.builder()
                    .eventId(String.valueOf(UuidCreator.getTimeOrderedEpoch()))
                    .transactionId(transactionId)
                    .supplierCompanyId(supplierCompany.getId())
                    .supplierCompanyCode(supplierCompany.getCompanyCode())
                    .supplierCompanyName(supplierCompany.getCompanyName())
                    .supplierUserId(supplierUser.getId())
                    .userId(externalUserId)
                    .managerName(supplierUser.getSupplierUserName())
                    .managerEmail(supplierUser.getSupplierUserEmail())
                    .managerPhone(supplierUser.getSupplierUserPhoneNumber())
                    .build();

                supplierUserServicePort.createSupplierUser(event)
                    .exceptionally(error -> {
                        log.error("[SAGA][SUPPLIER][FAIL] 공급사 계정 생성 이벤트 발행 실패 - txId: {}, cause: {}", transactionId, error.getMessage(), error);
                        asyncResultManager.setErrorResult(
                            transactionId,
                            "[SAGA][FAIL] 공급사 계정 생성 요청 발행 실패: " + error.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR
                        );
                        return null;
                    });

                log.info("[SAGA][SUPPLIER] 공급사 등록 처리 완료 - txId: {}, supplierId: {}", transactionId, supplierCompany.getId());
                return null;
            } catch (Exception error) {
                log.error("[SAGA][SUPPLIER][FAIL] 공급사 등록 처리 실패 - txId: {}, cause: {}", transactionId, error.getMessage(), error);
                asyncResultManager.setErrorResult(
                    transactionId,
                    "[SAGA][FAIL] 공급사 등록 처리에 실패했습니다.: " + error.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
                );
                throw error;
            }
        });
    }

    private SupplierCreationResult saveSupplier(SupplierCreateRequestDto dto, String externalUserId) {
        SupplierCreateRequestDto.SupplierInfo info = dto.getSupplierInfo();
        SupplierCreateRequestDto.ManagerInfo manager = dto.getManagerInfo();

        SupplierCompany supplierCompany = SupplierCompany.builder()
            .companyCode(generateSupplierCode())
            .companyName(info.getSupplierName())
            .status("ACTIVE")
            .baseAddress(info.getSupplierBaseAddress())
            .detailAddress(info.getSupplierDetailAddress())
            .category(info.getCategory())
            .officePhone(info.getSupplierPhone())
            .deliveryDays(info.getDeliveryLeadTime())
            .build();

        SupplierCompany savedCompany = supplierCompanyRepository.save(supplierCompany);

        SupplierUser supplierUser = SupplierUser.builder()
            .userId(externalUserId)
            .supplierUserName(manager.getManagerName())
            .supplierUserEmail(manager.getManagerEmail())
            .supplierUserPhoneNumber(manager.getManagerPhone())
            .build();

        SupplierUser savedUser = supplierUserRepository.save(supplierUser);
        savedCompany.assignSupplierUser(savedUser);
        supplierCompanyRepository.save(savedCompany);

        return new SupplierCreationResult(savedCompany, savedUser);
    }

    private String generateSupplierCode() {
        String id = String.valueOf(UuidCreator.getTimeOrderedEpoch());
        String suffix = id.length() > 6 ? id.substring(id.length() - 6) : id;
        return "SUP-" + suffix;
    }
}
