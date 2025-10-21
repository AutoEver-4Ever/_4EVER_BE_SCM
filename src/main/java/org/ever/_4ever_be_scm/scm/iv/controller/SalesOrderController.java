package org.ever._4ever_be_scm.scm.iv.controller;

import lombok.RequiredArgsConstructor;
import org.ever._4ever_be_scm.common.response.ApiResponse;
import org.ever._4ever_be_scm.scm.iv.dto.PagedResponseDto;
import org.ever._4ever_be_scm.scm.iv.dto.SalesOrderDetailDto;
import org.ever._4ever_be_scm.scm.iv.dto.SalesOrderDto;
import org.ever._4ever_be_scm.scm.iv.service.SalesOrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 판매 주문 관리 컨트롤러
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SalesOrderController {
    
    private final SalesOrderService salesOrderService;
    
    /**
     * 생산중/출고준비완료/배송중 목록 조회 API
     * 
     * @param status 상태 (생산중, 출고준비완료, 배송중)
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 생산중/출고준비완료/배송중 목록
     */
    @GetMapping("/sales-orders/production")
    public ResponseEntity<ApiResponse<PagedResponseDto<SalesOrderDto>>> getSalesOrdersInProduction(
            @RequestParam(required = false, defaultValue = "생산중") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        // 서비스 호출
        Page<SalesOrderDto> salesOrdersPage = salesOrderService.getSalesOrdersByStatus(status, PageRequest.of(page, size));
        PagedResponseDto<SalesOrderDto> response = PagedResponseDto.from(salesOrdersPage);
        
        String message = "";
        if (status.equals("생산중")) {
            message = "생산중 주문 목록을 조회했습니다.";
        } else if (status.equals("출고준비완료")) {
            message = "출고 준비완료 주문 목록을 조회했습니다.";
        }
        
        return ResponseEntity.ok(ApiResponse.success(response, message, HttpStatus.OK));
    }
    
    /**
     * 출고 준비 완료 상세 조회 API
     * 
     * @param salesOrderId 판매 주문 ID
     * @return 출고 준비 완료 상세 정보
     */
    @GetMapping("/sales-orders/ready-to-ship/{salesOrderId}")
    public ResponseEntity<ApiResponse<SalesOrderDetailDto>> getReadyToShipOrder(@PathVariable String salesOrderId) {
        // 서비스 호출
        SalesOrderDetailDto salesOrderDetail = salesOrderService.getReadyToShipOrderDetail(salesOrderId);
        
        return ResponseEntity.ok(ApiResponse.success(salesOrderDetail, "출고 준비 완료 주문 상세를 조회했습니다.", HttpStatus.OK));
    }
}
