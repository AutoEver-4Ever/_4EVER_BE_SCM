package org.ever._4ever_be_scm.scm.iv.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.ever._4ever_be_scm.common.response.ApiResponse;
import org.ever._4ever_be_scm.scm.iv.dto.PagedResponseDto;
import org.ever._4ever_be_scm.scm.iv.dto.WarehouseDetailDto;
import org.ever._4ever_be_scm.scm.iv.dto.WarehouseDto;
import org.ever._4ever_be_scm.scm.iv.service.WarehouseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 창고 관리 컨트롤러
 */
@Tag(name = "재고관리", description = "재고 관리 API")
@RestController
@RequestMapping("/scm-pp/iv/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;
    
    /**
     * 창고 목록 조회 API
     * 
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 창고 목록
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponseDto<WarehouseDto>>> getWarehouses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<WarehouseDto> warehouses = warehouseService.getWarehouses(PageRequest.of(page, size));
        PagedResponseDto<WarehouseDto> response = PagedResponseDto.from(warehouses);
        return ResponseEntity.ok(ApiResponse.success(response, "창고 목록을 조회했습니다.", HttpStatus.OK));
    }
    
    /**
     * 창고 상세 정보 조회 API
     * 
     * @param warehouseId 창고 ID
     * @return 창고 상세 정보
     */
    @GetMapping("/{warehouseId}")
    public ResponseEntity<ApiResponse<WarehouseDetailDto>> getWarehouseDetail(@PathVariable String warehouseId) {
        WarehouseDetailDto warehouseDetail = warehouseService.getWarehouseDetail(warehouseId);
        return ResponseEntity.ok(ApiResponse.success(warehouseDetail, "창고 상세 정보를 조회했습니다.", HttpStatus.OK));
    }
}
