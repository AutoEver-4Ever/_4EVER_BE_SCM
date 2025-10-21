package org.ever._4ever_be_scm.scm.controller;

import lombok.RequiredArgsConstructor;
import org.ever._4ever_be_scm.common.response.ApiResponse;
import org.ever._4ever_be_scm.scm.dto.InventoryItemDetailDto;
import org.ever._4ever_be_scm.scm.dto.InventoryItemDto;
import org.ever._4ever_be_scm.scm.dto.PagedResponseDto;
import org.ever._4ever_be_scm.scm.dto.ShortageItemDto;
import org.ever._4ever_be_scm.scm.dto.ShortageItemPreviewDto;
import org.ever._4ever_be_scm.scm.service.InventoryService;
import org.ever._4ever_be_scm.scm.vo.InventoryFilterVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scm-pp/iv")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * 재고 목록 조회 API
     * 
     * @param category 제품 카테고리 필터
     * @param status 재고 상태 필터
     * @param warehouseId 창고ID 필터
     * @param itemName 제품명 검색
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 재고 목록
     */
    @GetMapping("/items")
    public ResponseEntity<ApiResponse<PagedResponseDto<InventoryItemDto>>> getInventoryItems(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String warehouseId,
            @RequestParam(required = false) String itemName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // VO 객체 생성 및 서비스 호출
        InventoryFilterVo filterVo = InventoryFilterVo.builder()
                .category(category)
                .status(status)
                .warehouseId(warehouseId)
                .itemName(itemName)
                .build();
        
        Page<InventoryItemDto> items = inventoryService.getInventoryItems(filterVo, PageRequest.of(page, size));
        PagedResponseDto<InventoryItemDto> response = PagedResponseDto.from(items);
        
        return ResponseEntity.ok(ApiResponse.success(response, "재고 목록을 조회했습니다.", HttpStatus.OK));
    }

    /**
     * 재고 상세 정보 조회 API
     * 
     * @param itemId 재고 ID
     * @return 재고 상세 정보
     */
    @GetMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<InventoryItemDetailDto>> getInventoryItemDetail(@PathVariable String itemId) {
        InventoryItemDetailDto itemDetail = inventoryService.getInventoryItemDetail(itemId);
        
        return ResponseEntity.ok(ApiResponse.success(itemDetail, "재고 상세 정보를 조회했습니다.", HttpStatus.OK));
    }

    /**
     * 부족 재고 목록 조회 API
     * 
     * @param status 재고 상태 필터
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 부족 재고 목록
     */
    @GetMapping("/shortage")
    public ResponseEntity<ApiResponse<PagedResponseDto<ShortageItemDto>>> getShortageItems(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<ShortageItemDto> items = inventoryService.getShortageItems(status, PageRequest.of(page, size));
        PagedResponseDto<ShortageItemDto> response = PagedResponseDto.from(items);
        
        return ResponseEntity.ok(ApiResponse.success(response, "부족 재고 목록을 조회했습니다.", HttpStatus.OK));
    }

    /**
     * 부족 재고 간단 정보 조회 API
     *
     * @return 부족 재고 간단 정보 목록
     */
    @GetMapping("/shortage/preview")
    public ResponseEntity<ApiResponse<PagedResponseDto<ShortageItemPreviewDto>>> getShortageItemsPreview() {
        Page<ShortageItemPreviewDto> items = inventoryService.getShortageItemsPreview(PageRequest.of(0, 5));
        PagedResponseDto<ShortageItemPreviewDto> response = PagedResponseDto.from(items);
        
        return ResponseEntity.ok(ApiResponse.success(response, "재고 부족 목록을 조회했습니다.", HttpStatus.OK));
    }
}
