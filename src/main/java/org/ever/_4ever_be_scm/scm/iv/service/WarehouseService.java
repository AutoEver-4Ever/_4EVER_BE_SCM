package org.ever._4ever_be_scm.scm.iv.service;

import org.ever._4ever_be_scm.scm.iv.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 창고 관리 서비스 인터페이스
 */
public interface WarehouseService {
    /**
     * 창고 목록 조회
     * 
     * @param pageable 페이징 정보
     * @return 창고 목록
     */
    Page<WarehouseDto> getWarehouses(Pageable pageable);
    
    /**
     * 창고 상세 정보 조회
     * 
     * @param warehouseId 창고 ID
     * @return 창고 상세 정보
     */
    WarehouseDetailDto getWarehouseDetail(String warehouseId);
    
    /**
     * 창고 생성
     * 
     * @param request 창고 생성 요청 정보
     */
    void createWarehouse(WarehouseCreateRequestDto request);
    
    /**
     * 창고 정보 수정
     * 
     * @param warehouseId 창고 ID
     * @param request 창고 수정 요청 정보
     */
    void updateWarehouse(String warehouseId, WarehouseUpdateRequestDto request);
    
    /**
     * 창고 드롭다운 목록 조회
     * 
     * @param excludeWarehouseId 제외할 창고 ID (선택사항)
     * @return 창고 드롭다운 목록
     */
    WarehouseDropdownResponseDto getWarehouseDropdown(String excludeWarehouseId);
}
