package org.ever._4ever_be_scm.scm.iv.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.ever._4ever_be_scm.common.response.ApiResponse;
import org.ever._4ever_be_scm.scm.iv.dto.InventoryItemDetailDto;
import org.ever._4ever_be_scm.scm.iv.dto.InventoryItemDto;
import org.ever._4ever_be_scm.scm.iv.dto.PagedResponseDto;
import org.ever._4ever_be_scm.scm.iv.dto.ShortageItemDto;
import org.ever._4ever_be_scm.scm.iv.dto.ShortageItemPreviewDto;
import org.ever._4ever_be_scm.scm.iv.dto.request.AddInventoryItemRequest;
import org.ever._4ever_be_scm.scm.iv.service.InventoryService;
import org.ever._4ever_be_scm.scm.iv.vo.InventoryFilterVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "재고관리", description = "재고 관리 API")
@RestController
@RequestMapping("/scm-pp/iv")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * 재고 목록 조회 API
     * 
     * @param type 검색 타입: WAREHOUSE_NAME 또는 ITEM_NAME
     * @param keyword 검색 키워드
     * @param statusCode 재고 상태: ALL, NORMAL, CAUTION, URGENT
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 재고 목록
     */
    @GetMapping("/inventory-items")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "재고 목록 조회",
            description = "재고 목록을 조회합니다. 타입(WAREHOUSE_NAME, ITEM_NAME)과 키워드로 검색, 상태코드(ALL, NORMAL, CAUTION, URGENT) 필터링 가능"
    )
    public ResponseEntity<ApiResponse<Object>> getInventoryItems(
            @io.swagger.v3.oas.annotations.Parameter(description = "검색 타입: WAREHOUSE_NAME 또는 ITEM_NAME")
            @RequestParam(name = "type", required = false) String type,
            @io.swagger.v3.oas.annotations.Parameter(description = "검색 키워드")
            @RequestParam(name = "keyword", required = false) String keyword,
            @io.swagger.v3.oas.annotations.Parameter(description = "재고 상태: ALL, NORMAL, CAUTION, URGENT")
            @RequestParam(name = "statusCode", required = false, defaultValue = "ALL") String statusCode,
            @io.swagger.v3.oas.annotations.Parameter(description = "페이지 번호")
            @RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
            @io.swagger.v3.oas.annotations.Parameter(description = "페이지 크기")
            @RequestParam(name = "size", required = false, defaultValue = "10") Integer size
    ) {
        
        PagedResponseDto<InventoryItemDto> response = inventoryService.getInventoryItemsWithFilters(type, keyword, statusCode, page, size);
        
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("content", response.getContent());
        result.put("page", response.getPage());
        
        return ResponseEntity.ok(ApiResponse.success(result, "재고 목록을 조회했습니다.", HttpStatus.OK));
    }

    /**
     * 재고 추가 API
     * 
     * @param request 재고 추가 요청
     * @return 추가 결과
     */
    @PostMapping("/items")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "재고 추가",
            description = "product 테이블에는 존재하지만 productStock에 존재하지 않는 제품을 productStock에 추가합니다."
    )
    public ResponseEntity<ApiResponse<Void>> addInventoryItem(
            @RequestBody AddInventoryItemRequest request) {
        
        inventoryService.addInventoryItem(request);
        
        return ResponseEntity.ok(ApiResponse.success(null, "재고가 추가되었습니다.", HttpStatus.OK));
    }

    /**
     * 안전재고 수정 API
     * 
     * @param itemId 제품 ID
     * @param safetyStock 안전재고 수량
     * @return 수정 결과
     */
    @PatchMapping("/items/{itemId}/safety-stock")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "안전재고 수정",
            description = "itemId(productId)를 받아서 productStock의 safetyStock값을 변경합니다."
    )
    public ResponseEntity<ApiResponse<Void>> updateSafetyStock(
            @PathVariable String itemId,
            @RequestParam Integer safetyStock) {
        
        inventoryService.updateSafetyStock(itemId, safetyStock);
        
        return ResponseEntity.ok(ApiResponse.success(null, "안전재고가 수정되었습니다.", HttpStatus.OK));
    }

    /**
     * 재고 상세 정보 조회 API
     * 
     * @param itemId 재고 ID
     * @return 재고 상세 정보
     */
    @GetMapping("/items/{itemId}")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "재고 상세 조회"
    )
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
    @io.swagger.v3.oas.annotations.Operation(
            summary = "부족 재고 목록 조회"
    )
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
    @io.swagger.v3.oas.annotations.Operation(
            summary = "부족 재고 간단 조회"
    )
    public ResponseEntity<ApiResponse<PagedResponseDto<ShortageItemPreviewDto>>> getShortageItemsPreview() {
        Page<ShortageItemPreviewDto> items = inventoryService.getShortageItemsPreview(PageRequest.of(0, 5));
        PagedResponseDto<ShortageItemPreviewDto> response = PagedResponseDto.from(items);
        
        return ResponseEntity.ok(ApiResponse.success(response, "재고 부족 목록을 조회했습니다.", HttpStatus.OK));
    }
}
