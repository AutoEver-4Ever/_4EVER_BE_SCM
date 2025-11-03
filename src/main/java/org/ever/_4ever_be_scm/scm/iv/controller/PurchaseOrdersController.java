package org.ever._4ever_be_scm.scm.iv.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.ever._4ever_be_scm.common.response.ApiResponse;
import org.ever._4ever_be_scm.scm.iv.dto.PagedResponseDto;
import org.ever._4ever_be_scm.scm.iv.dto.PurchaseOrderDto;
import org.ever._4ever_be_scm.scm.iv.service.PurchaseOrdersService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 구매 발주 관리 컨트롤러
 */
@Tag(name = "재고관리", description = "재고 관리 API")
@RestController
@RequestMapping("/scm-pp")
@RequiredArgsConstructor
public class PurchaseOrdersController {
    
    private final PurchaseOrdersService purchaseOrderService;
    
    /**
     * 입고 준비 목록 조회 API
     * 
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 입고 준비 목록
     */
    @GetMapping("/purchase-orders/receiving")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "입고 준비 목록 조회"
    )
    public ResponseEntity<ApiResponse<PagedResponseDto<PurchaseOrderDto>>> getReceivingPurchaseOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        // 서비스 호출 - DELIVERING 상태만 조회
        Page<PurchaseOrderDto> purchaseOrdersPage = purchaseOrderService.getReceivingPurchaseOrders(PageRequest.of(page, size));
        PagedResponseDto<PurchaseOrderDto> response = PagedResponseDto.from(purchaseOrdersPage);
        
        return ResponseEntity.ok(ApiResponse.success(response, "입고 준비 목록을 조회했습니다.", HttpStatus.OK));
    }
    
    /**
     * 입고 완료 목록 조회 API
     * 
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @param startDate 시작일 (yyyy-MM-dd)
     * @param endDate 종료일 (yyyy-MM-dd)
     * @return 입고 완료 목록
     */
    @GetMapping("/purchase-orders/received")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "입고 완료 목록 조회"
    )
    public ResponseEntity<ApiResponse<PagedResponseDto<PurchaseOrderDto>>> getReceivedPurchaseOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        // 서비스 호출 - DELIVERED 상태만 조회, dueDate 기준 필터링
        Page<PurchaseOrderDto> purchaseOrdersPage = purchaseOrderService.getReceivedPurchaseOrders(
                PageRequest.of(page, size), startDate, endDate);
        PagedResponseDto<PurchaseOrderDto> response = PagedResponseDto.from(purchaseOrdersPage);
        
        return ResponseEntity.ok(ApiResponse.success(response, "입고 완료 목록을 조회했습니다.", HttpStatus.OK));
    }
}
