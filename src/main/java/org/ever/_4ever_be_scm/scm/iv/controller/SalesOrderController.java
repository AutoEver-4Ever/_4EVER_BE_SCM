package org.ever._4ever_be_scm.scm.iv.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "재고관리", description = "재고 관리 API")
@RestController
@RequestMapping("/scm-pp")
@RequiredArgsConstructor
public class SalesOrderController {
    
    private final SalesOrderService salesOrderService;
    
    /**
     * 생산중 목록 조회 API
     * 
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 생산중 목록
     */
    @GetMapping("/sales-orders/production")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "생산중 목록 조회"
    )
    public ResponseEntity<ApiResponse<PagedResponseDto<SalesOrderDto>>> getProductionSalesOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        // 서비스 호출 - IN_PRODUCTION 상태만 조회
        Page<SalesOrderDto> salesOrdersPage = salesOrderService.getProductionSalesOrders(PageRequest.of(page, size));
        PagedResponseDto<SalesOrderDto> response = PagedResponseDto.from(salesOrdersPage);
        
        return ResponseEntity.ok(ApiResponse.success(response, "생산중 주문 목록을 조회했습니다.", HttpStatus.OK));
    }
    
    /**
     * 출고 준비완료 목록 조회 API
     * 
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 출고 준비완료 목록
     */
    @GetMapping("/sales-orders/ready-to-ship")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "출고 준비완료 목록 조회"
    )
    public ResponseEntity<ApiResponse<PagedResponseDto<SalesOrderDto>>> getReadyToShipSalesOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        // 서비스 호출 - READY_TO_SHIP 상태만 조회
        Page<SalesOrderDto> salesOrdersPage = salesOrderService.getReadyToShipSalesOrders(PageRequest.of(page, size));
        PagedResponseDto<SalesOrderDto> response = PagedResponseDto.from(salesOrdersPage);
        
        return ResponseEntity.ok(ApiResponse.success(response, "출고 준비완료 주문 목록을 조회했습니다.", HttpStatus.OK));
    }
    
    /**
     * 출고 준비 완료 상세 조회 API
     * 
     * @param salesOrderId 판매 주문 ID
     * @return 출고 준비 완료 상세 정보
     */
    @GetMapping("/sales-orders/ready-to-ship/{salesOrderId}")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "출고 준비완료 상세 조회"
    )
    public ResponseEntity<ApiResponse<SalesOrderDetailDto>> getReadyToShipOrder(@PathVariable String salesOrderId) {
        // 서비스 호출
        SalesOrderDetailDto salesOrderDetail = salesOrderService.getReadyToShipOrderDetail(salesOrderId);
        
        return ResponseEntity.ok(ApiResponse.success(salesOrderDetail, "출고 준비 완료 주문 상세를 조회했습니다.", HttpStatus.OK));
    }

    /**
     * 생산중 상세 조회 API
     *
     * @param salesOrderId 판매 주문 ID
     * @return 생산중 상세 정보
     */
    @GetMapping("/sales-orders/production/{salesOrderId}")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "생산중 상세 조회"
    )
    public ResponseEntity<ApiResponse<SalesOrderDetailDto>> getProductionOrder(@PathVariable String salesOrderId) {
        // 서비스 호출
        SalesOrderDetailDto salesOrderDetail = salesOrderService.getProductionDetail(salesOrderId);

        return ResponseEntity.ok(ApiResponse.success(salesOrderDetail, "출고 준비 완료 주문 상세를 조회했습니다.", HttpStatus.OK));
    }
}
