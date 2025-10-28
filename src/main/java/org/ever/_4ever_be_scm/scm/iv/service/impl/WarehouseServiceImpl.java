package org.ever._4ever_be_scm.scm.iv.service.impl;

import lombok.RequiredArgsConstructor;
import org.ever._4ever_be_scm.scm.iv.dto.*;
import org.ever._4ever_be_scm.scm.iv.entity.Warehouse;
import org.ever._4ever_be_scm.scm.iv.repository.WarehouseRepository;
import org.ever._4ever_be_scm.scm.iv.service.WarehouseService;
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
                .warehouseType(warehouse.getWarehouseType())
                .statusCode(warehouse.getStatus())
                .location(warehouse.getLocation())
                .description(warehouse.getDescription())
                .build();

        // todo user 연결하면 추가
        // 담당자 정보 구성 (현재 구조에서는 내부 사용자 ID만 있음)
        // 실제 구현에서는 내부 사용자 저장소를 통해 조회해야 함
        String managerId = "123";
        String managerName = "미지정";
        String managerPhone = "-";
        String managerEmail = "-";
        
        if (warehouse.getInternalUserId() != null) {
            // 실제 구현에서는 아래와 같이 저장소를 통해 조회
            // InternalUser manager = internalUserRepository.findById(warehouse.getInternalUserId()).orElse(null);
            // if (manager != null) {
            //     managerName = manager.getName();
            //     phoneNumber = manager.getPhone();
            //     email = manager.getEmail();
            // }
            
            // 간단한 구현을 위해 하드코딩
            managerName = "김창고";
            managerPhone = "031-123-4567";
            managerEmail = "manager@everp.com";
        }
        
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

        // todo 담당자 연결
        // 담당자 정보는 실제 구현에서는 별도 저장소 조회 필요
        String managerName = "미지정";
        String phoneNumber = "-";
        
        if (warehouse.getInternalUserId() != null) {
            // 실제 구현에서는 내부 사용자 저장소를 통해 조회해야 함
            // 간단한 구현을 위해 하드코딩
            managerName = "김창고";
            phoneNumber = "031-123-4567";
        }
        
        return WarehouseDto.builder()
                .warehouseId(warehouse.getId())
                .warehouseNumber(warehouse.getWarehouseCode())
                .warehouseName(warehouse.getWarehouseName())
                .statusCode(warehouse.getStatus())
                .warehouseType(warehouse.getWarehouseType())
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
}
