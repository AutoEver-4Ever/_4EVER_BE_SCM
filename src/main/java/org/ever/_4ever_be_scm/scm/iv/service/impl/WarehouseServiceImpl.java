package org.ever._4ever_be_scm.scm.iv.service.impl;

import lombok.RequiredArgsConstructor;
import org.ever._4ever_be_scm.common.exception.BusinessException;
import org.ever._4ever_be_scm.scm.iv.dto.*;
import org.ever._4ever_be_scm.scm.iv.entity.Warehouse;
import org.ever._4ever_be_scm.scm.iv.repository.WarehouseRepository;
import org.ever._4ever_be_scm.scm.iv.service.WarehouseService;
import org.ever._4ever_be_scm.scm.mm.integration.dto.InternalUserResponseDto;
import org.ever._4ever_be_scm.scm.mm.integration.port.InternalUserServicePort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 창고 관리 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final InternalUserServicePort internalUserServicePort;
    
    /**
     * 창고 목록 조회
     * 
     * @param pageable 페이징 정보
     * @return 창고 목록
     */
    @Override
    public Page<WarehouseDto> getWarehouses(Pageable pageable) {
        Page<Warehouse> warehouses = warehouseRepository.findAll(pageable);
        return warehouses.map(this::mapToWarehouseDto);
    }
    
    /**
     * 창고 상세 정보 조회
     * 
     * @param warehouseId 창고 ID
     * @return 창고 상세 정보
     */
    @Override
    public WarehouseDetailDto getWarehouseDetail(String warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new NoSuchElementException("창고를 찾을 수 없습니다. ID: " + warehouseId));
        
        // 기본 창고 정보 구성
        WarehouseDetailDto.WarehouseInfoDto warehouseInfo = WarehouseDetailDto.WarehouseInfoDto.builder()
                .warehouseName(warehouse.getWarehouseName())
                .warehouseNumber(warehouse.getWarehouseCode())
                .warehouseType(mapCategory(warehouse.getWarehouseType()))
                .statusCode(warehouse.getStatus())
                .location(warehouse.getLocation())
                .description(warehouse.getDescription())
                .build();

        InternalUserResponseDto managerInfo  = internalUserServicePort.getInternalUserInfoById(warehouse.getInternalUserId());

        String managerId = managerInfo.getUserId();
        String managerName = managerInfo.getName();
        String managerPhone = managerInfo.getPhoneNumber();
        String managerEmail = managerInfo.getEmail();
        
        WarehouseDetailDto.ManagerDto manager = WarehouseDetailDto.ManagerDto.builder()
                .managerId(managerId)
                .managerName(managerName)
                .managerPhone(managerPhone)
                .managerEmail(managerEmail)
                .build();
        
        // 최종 DTO 구성 및 반환
        return WarehouseDetailDto.builder()
                .warehouseInfo(warehouseInfo)
                .manager(manager)
                .build();
    }
    
    /**
     * Warehouse 엔티티를 WarehouseDto로 변환
     */
    private WarehouseDto mapToWarehouseDto(Warehouse warehouse) {

        InternalUserResponseDto managerInfo = internalUserServicePort.getInternalUserInfoById(warehouse.getInternalUserId());

        // todo 담당자 연결
        // 담당자 정보는 실제 구현에서는 별도 저장소 조회 필요
        String managerName = managerInfo.getName();
        String phoneNumber = managerInfo.getPhoneNumber();
        
        return WarehouseDto.builder()
                .warehouseId(warehouse.getId())
                .warehouseNumber(warehouse.getWarehouseCode())
                .warehouseName(warehouse.getWarehouseName())
                .statusCode(warehouse.getStatus())
                .warehouseType(mapCategory(warehouse.getWarehouseType()))
                .location(warehouse.getLocation())
                .manager(managerName)
                .managerPhone(phoneNumber)
                .build();
    }
    
    /**
     * 창고 생성
     * 
     * @param request 창고 생성 요청 정보
     */
    @Override
    @Transactional
    public void createWarehouse(WarehouseCreateRequestDto request) {
        // 창고 코드 자동 생성 (WH + 타임스탬프)
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String warehouseCode = "WH-" + uuid.substring(uuid.length() - 6);
        
        Warehouse warehouse = Warehouse.builder()
                .warehouseCode(warehouseCode)
                .warehouseName(request.getWarehouseName())
                .warehouseType(request.getWarehouseType())
                .status("ACTIVE") // 기본값으로 ACTIVE 설정
                .internalUserId(request.getManagerId())
                .location(request.getLocation())
                .description(request.getNote())
                .build();
        
        warehouseRepository.save(warehouse);
    }
    
    /**
     * 창고 정보 수정
     * 
     * @param warehouseId 창고 ID
     * @param request 창고 수정 요청 정보
     */
    @Override
    @Transactional
    public void updateWarehouse(String warehouseId, WarehouseUpdateRequestDto request) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new NoSuchElementException("창고를 찾을 수 없습니다."));

        if(!warehouse.getWarehouseType().equals(request.getWarehouseType())) {
            throw new RuntimeException("창고 유형은 변경이 불가능 합니다");
        }
        
        // 부분 업데이트 - null이 아닌 값만 업데이트
        Warehouse updatedWarehouse = Warehouse.builder()
                .id(warehouse.getId())
                .warehouseCode(warehouse.getWarehouseCode())
                .warehouseName(StringUtils.hasText(request.getWarehouseName()) ? 
                        request.getWarehouseName() : warehouse.getWarehouseName())
                .warehouseType(StringUtils.hasText(request.getWarehouseType()) ? 
                        request.getWarehouseType() : warehouse.getWarehouseType())
                .status(StringUtils.hasText(request.getWarehouseStatusCode()) ? 
                        request.getWarehouseStatusCode() : warehouse.getStatus())
                .internalUserId(StringUtils.hasText(request.getManagerId()) ? 
                        request.getManagerId() : warehouse.getInternalUserId())
                .location(StringUtils.hasText(request.getLocation()) ? 
                        request.getLocation() : warehouse.getLocation())
                .description(StringUtils.hasText(request.getNote()) ? 
                        request.getNote() : warehouse.getDescription())
                .build();
        
        warehouseRepository.save(updatedWarehouse);
    }
    
    /**
     * 창고 드롭다운 목록 조회
     * 
     * @param excludeWarehouseId 제외할 창고 ID (선택사항)
     * @return 창고 드롭다운 목록
     */
    @Override
    public WarehouseDropdownResponseDto getWarehouseDropdown(String excludeWarehouseId) {
        List<Warehouse> warehouses = warehouseRepository.findAllByStatus("ACTIVE");
        
        List<WarehouseDropdownResponseDto.WarehouseDropdownItem> items = warehouses.stream()
                .filter(warehouse -> excludeWarehouseId == null || !warehouse.getId().equals(excludeWarehouseId))
                .map(warehouse -> WarehouseDropdownResponseDto.WarehouseDropdownItem.builder()
                        .warehouseNumber(warehouse.getWarehouseCode())
                        .warehouseId(warehouse.getId())
                        .warehouseName(warehouse.getWarehouseName())
                        .build())
                .collect(Collectors.toList());
        
        return WarehouseDropdownResponseDto.builder()
                .warehouses(items)
                .build();
    }

    /**
     * 타입 or 카테고리 변환
     */
    private String mapCategory(String category) {
        if (category == null) return "기타";

        switch (category) {
            case "ITEM":
                return "부품";
            case "MATERIAL":
                return "원자재";
            case "ETC":
            default:
                return "기타";
        }
    }
}
