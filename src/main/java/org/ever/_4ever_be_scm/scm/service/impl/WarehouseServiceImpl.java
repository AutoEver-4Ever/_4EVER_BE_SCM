package org.ever._4ever_be_scm.scm.service.impl;

import lombok.RequiredArgsConstructor;
import org.ever._4ever_be_scm.scm.dto.WarehouseDetailDto;
import org.ever._4ever_be_scm.scm.dto.WarehouseDto;
import org.ever._4ever_be_scm.scm.entity.Warehouse;
import org.ever._4ever_be_scm.scm.repository.WarehouseRepository;
import org.ever._4ever_be_scm.scm.service.WarehouseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

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
                .warehouseCode(warehouse.getWarehouseCode())
                .warehouseType(warehouse.getWarehouseType())
                .warehouseStatus(warehouse.getStatus())
                .location(warehouse.getLocation())
                .description(warehouse.getDescription())
                .build();

        // todo user 연결하면 추가
        // 담당자 정보 구성 (현재 구조에서는 내부 사용자 ID만 있음)
        // 실제 구현에서는 내부 사용자 저장소를 통해 조회해야 함
        String managerName = "미지정";
        String phoneNumber = "-";
        String email = "-";
        
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
            phoneNumber = "031-123-4567";
            email = "manager@everp.com";
        }
        
        WarehouseDetailDto.ManagerDto manager = WarehouseDetailDto.ManagerDto.builder()
                .name(managerName)
                .phoneNumber(phoneNumber)
                .email(email)
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
                .warehouseCode(warehouse.getWarehouseCode())
                .warehouseName(warehouse.getWarehouseName())
                .status(warehouse.getStatus())
                .warehouseType(warehouse.getWarehouseType())
                .location(warehouse.getLocation())
                .manager(managerName)
                .phone(phoneNumber)
                .build();
    }
}
