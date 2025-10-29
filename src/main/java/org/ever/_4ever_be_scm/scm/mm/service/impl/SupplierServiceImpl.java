package org.ever._4ever_be_scm.scm.mm.service.impl;

import lombok.RequiredArgsConstructor;
import org.ever._4ever_be_scm.scm.iv.entity.SupplierCompany;
import org.ever._4ever_be_scm.scm.iv.entity.SupplierUser;
import org.ever._4ever_be_scm.scm.iv.repository.SupplierCompanyRepository;
import org.ever._4ever_be_scm.scm.mm.dto.SupplierDetailResponseDto;
import org.ever._4ever_be_scm.scm.mm.dto.SupplierListResponseDto;
import org.ever._4ever_be_scm.scm.mm.service.SupplierService;
import org.ever._4ever_be_scm.scm.mm.vo.SupplierSearchVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {
    
    private final SupplierCompanyRepository supplierCompanyRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<SupplierListResponseDto> getSupplierList(SupplierSearchVo searchVo) {
        PageRequest pageRequest = PageRequest.of(searchVo.getPage(), searchVo.getSize());
        
        // 실제 SupplierCompany 엔티티의 status, category, supplierUser를 사용해서 DTO 생성
        Page<SupplierCompany> supplierCompanies = supplierCompanyRepository.findAll(pageRequest);

        List<SupplierListResponseDto> dtoList = new ArrayList<>();
        for (SupplierCompany supplierCompany : supplierCompanies.getContent()) {
            String category = supplierCompany.getCategory();
            String status = supplierCompany.getStatus();

            if(status == null || category == null){
                continue;
            }

            // 검색 필터 적용
            if (!"ALL".equals(searchVo.getStatusCode()) && !status.equals(searchVo.getStatusCode())) {
                continue;
            }
            if (!"ALL".equals(searchVo.getCategory()) && !category.equals(searchVo.getCategory())) {
                continue;
            }
            
            // type과 keyword 검색 필터 추가
            if (searchVo.getType() != null && searchVo.getKeyword() != null && !searchVo.getKeyword().trim().isEmpty()) {
                boolean matchesSearch = false;
                String keyword = searchVo.getKeyword().toLowerCase();
                
                if ("SupplierCompanyNumber".equals(searchVo.getType())) {
                    // 공급업체 번호로 검색
                    if (supplierCompany.getCompanyCode() != null && 
                        supplierCompany.getCompanyCode().toLowerCase().contains(keyword)) {
                        matchesSearch = true;
                    }
                } else if ("SupplierCompanyName".equals(searchVo.getType())) {
                    // 공급업체명으로 검색
                    if (supplierCompany.getCompanyName() != null && 
                        supplierCompany.getCompanyName().toLowerCase().contains(keyword)) {
                        matchesSearch = true;
                    }
                }
                
                if (!matchesSearch) {
                    continue;
                }
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

    // SupplierUser 정보 조회
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
}
