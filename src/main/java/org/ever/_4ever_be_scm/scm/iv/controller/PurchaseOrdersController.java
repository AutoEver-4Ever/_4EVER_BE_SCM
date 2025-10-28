package org.ever._4ever_be_scm.scm.iv.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.ever._4ever_be_scm.common.response.ApiResponse;
import org.ever._4ever_be_scm.scm.iv.dto.PagedResponseDto;
import org.ever._4ever_be_scm.scm.iv.dto.PurchaseOrderDto;
import org.ever._4ever_be_scm.scm.iv.service.PurchaseOrdersService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 구매 발주 관리 컨트롤러
 */
@Tag(name = "재고관리", description = "재고 관리 API")
@RestController
@RequestMapping("/api/scm-pp/iv")
@RequiredArgsConstructor
public class PurchaseOrdersController {
    
    private final PurchaseOrdersService purchaseOrderService;
    
    /**
     * 입고 대기/완료 목록 조회 API
     * 
     * @param status 상태 (입고 대기, 입고 완료)
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 입고 대기/완료 목록
     */
    @GetMapping("/purchase-orders")
    public ResponseEntity<ApiResponse<PagedResponseDto<PurchaseOrderDto>>> getPurchaseOrders(
            @RequestParam(required = false, defaultValue = "입고 대기") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        // 서비스 호출
        Page<PurchaseOrderDto> purchaseOrdersPage = purchaseOrderService.getPurchaseOrdersByStatus(status, PageRequest.of(page, size));
        PagedResponseDto<PurchaseOrderDto> response = PagedResponseDto.from(purchaseOrdersPage);
        
        String message = status.equals("입고 완료") ? "입고 완료 목록을 조회했습니다." : "입고 대기 목록을 조회했습니다.";
        
        return ResponseEntity.ok(ApiResponse.success(response, message, HttpStatus.OK));
    }
}
