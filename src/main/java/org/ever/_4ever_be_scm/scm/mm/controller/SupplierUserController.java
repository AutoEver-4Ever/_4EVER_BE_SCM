package org.ever._4ever_be_scm.scm.mm.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ever._4ever_be_scm.common.exception.BusinessException;
import org.ever._4ever_be_scm.common.exception.ErrorCode;
import org.ever._4ever_be_scm.common.response.ApiResponse;
import org.ever._4ever_be_scm.scm.iv.entity.SupplierCompany;
import org.ever._4ever_be_scm.scm.iv.entity.SupplierUser;
import org.ever._4ever_be_scm.scm.iv.repository.SupplierCompanyRepository;
import org.ever._4ever_be_scm.scm.iv.repository.SupplierUserRepository;
import org.ever._4ever_be_scm.scm.mm.dto.SupplierProfileResponseDto;
import org.ever._4ever_be_scm.scm.mm.dto.UserNameResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/scm-pp/mm/users")
@RequiredArgsConstructor
public class SupplierUserController {

    private final SupplierUserRepository supplierUserRepository;
    private final SupplierCompanyRepository supplierCompanyRepository;

    @GetMapping("/supplier/{userId}")
    public ApiResponse<UserNameResponseDto> getSupplierUserName(@PathVariable String userId) {
        log.info("공급사 담당자 이름 조회 요청 - userId: {}", userId);

        SupplierUser supplierUser = supplierUserRepository.findByUserId(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "공급사 담당자 정보를 찾을 수 없습니다."));

        UserNameResponseDto response = new UserNameResponseDto(supplierUser.getUserId(), supplierUser.getSupplierUserName());
        log.info("공급사 담당자 이름 조회 성공 - userId: {}, userName: {}", response.getUserId(), response.getUserName());

        return ApiResponse.success(response, "공급사 담당자 이름을 조회했습니다.", HttpStatus.OK);
    }

    @GetMapping("/supplier/{userId}/profile")
    public ApiResponse<SupplierProfileResponseDto> getSupplierProfile(@PathVariable String userId) {
        log.info("공급사 프로필 조회 요청 - userId: {}", userId);

        // SupplierUser 조회
        SupplierUser supplierUser = supplierUserRepository.findByUserId(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "공급사 담당자 정보를 찾을 수 없습니다."));

        // SupplierCompany 조회
        SupplierCompany supplierCompany = supplierCompanyRepository.findBySupplierUser(supplierUser)
            .orElseThrow(() -> new BusinessException(ErrorCode.SCM_NOT_FOUND, "공급사 회사 정보를 찾을 수 없습니다."));

        // 응답 DTO 생성
        SupplierProfileResponseDto response = SupplierProfileResponseDto.builder()
            .businessNumber(supplierCompany.getBusinessNumber())
            .supplierUserName(supplierUser.getSupplierUserName())
            .supplierUserEmail(supplierUser.getSupplierUserEmail())
            .supplierUserPhoneNumber(supplierUser.getSupplierUserPhoneNumber())
            .companyName(supplierCompany.getCompanyName())
            .baseAddress(supplierCompany.getBaseAddress())
            .detailAddress(supplierCompany.getDetailAddress())
            .officePhone(supplierCompany.getOfficePhone())
            .build();

        log.info("공급사 프로필 조회 성공 - userId: {}, companyName: {}", userId, supplierCompany.getCompanyName());

        return ApiResponse.success(response, "공급사 프로필을 조회했습니다.", HttpStatus.OK);
    }
}
