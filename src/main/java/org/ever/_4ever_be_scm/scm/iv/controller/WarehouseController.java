package org.ever._4ever_be_scm.scm.iv.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.ever._4ever_be_scm.common.response.ApiResponse;
import org.ever._4ever_be_scm.scm.iv.dto.*;
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
    @io.swagger.v3.oas.annotations.Operation(
            summary = "창고 목록 조회"
    )
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
    @io.swagger.v3.oas.annotations.Operation(
            summary = "창고 상세 조회"
    )
    public ResponseEntity<ApiResponse<WarehouseDetailDto>> getWarehouseDetail(@PathVariable String warehouseId) {
        WarehouseDetailDto warehouseDetail = warehouseService.getWarehouseDetail(warehouseId);
        return ResponseEntity.ok(ApiResponse.success(warehouseDetail, "창고 상세 정보를 조회했습니다.", HttpStatus.OK));
    }
    
    /**
     * 창고 추가 API
     * 
     * @param request 창고 생성 요청 정보
     * @return 생성된 창고 정보
     */
    @PostMapping
    @io.swagger.v3.oas.annotations.Operation(
            summary = "창고 추가"
    )
    public ResponseEntity<ApiResponse<String>> createWarehouse(@RequestBody WarehouseCreateRequestDto request) {
        try {
            warehouseService.createWarehouse(request);
            return ResponseEntity.ok(ApiResponse.success(null, "창고가 성공적으로 생성되었습니다.", HttpStatus.OK));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("창고 생성 실패: " + e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }
    
    /**
     * 창고 정보 수정 API
     * 
     * @param warehouseId 창고 ID
     * @param request 창고 수정 요청 정보
     * @return 수정 결과
     */
    @PutMapping("/{warehouseId}")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "창고 정보 수정"
    )
    public ResponseEntity<ApiResponse<String>> updateWarehouse(
            @PathVariable String warehouseId,
            @RequestBody WarehouseUpdateRequestDto request) {
        try {
            warehouseService.updateWarehouse(warehouseId, request);
            return ResponseEntity.ok(ApiResponse.success(null, "창고 정보가 성공적으로 수정되었습니다.", HttpStatus.OK));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("창고 수정 실패: " + e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }
    
    /**
     * 창고 드롭다운 목록 조회 API
     * 
     * @param warehouseId 제외할 창고 ID (선택사항)
     * @return 창고 드롭다운 목록
     */
    @GetMapping("/dropdown")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "창고 드롭다운 목록 조회"
    )
    public ResponseEntity<ApiResponse<WarehouseDropdownResponseDto>> getWarehouseDropdown(
            @RequestParam(required = false) String warehouseId) {
        WarehouseDropdownResponseDto dropdown = warehouseService.getWarehouseDropdown(warehouseId);
        return ResponseEntity.ok(ApiResponse.success(dropdown, "창고 드롭다운 목록을 조회했습니다.", HttpStatus.OK));
    }
}
