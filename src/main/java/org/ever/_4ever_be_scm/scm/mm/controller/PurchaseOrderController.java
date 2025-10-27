package org.ever._4ever_be_scm.scm.mm.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.ever._4ever_be_scm.common.response.ApiResponse;
import org.ever._4ever_be_scm.scm.iv.dto.PagedResponseDto;
import org.ever._4ever_be_scm.scm.mm.dto.PurchaseOrderDetailResponseDto;
import org.ever._4ever_be_scm.scm.mm.dto.PurchaseOrderListResponseDto;
import org.ever._4ever_be_scm.scm.mm.dto.PurchaseOrderRejectRequestDto;
import org.ever._4ever_be_scm.scm.mm.service.PurchaseOrderService;
import org.ever._4ever_be_scm.scm.mm.vo.PurchaseOrderSearchVo;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;


@Tag(name = "구매관리", description = "구매 관리 API")
@RestController
@RequestMapping("/api/scm-pp/mm/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {
    
    private final PurchaseOrderService purchaseOrderService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponseDto<PurchaseOrderListResponseDto>>> getPurchaseOrderList(
            @RequestParam(defaultValue = "ALL") String statusCode,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        PurchaseOrderSearchVo searchVo = PurchaseOrderSearchVo.builder()
                .statusCode(statusCode)
                .type(type)
                .keyword(keyword)
                .startDate(startDate)
                .endDate(endDate)
                .page(page)
                .size(size)
                .build();
        
        Page<PurchaseOrderListResponseDto> purchaseOrders = purchaseOrderService.getPurchaseOrderList(searchVo);
        PagedResponseDto<PurchaseOrderListResponseDto> response = PagedResponseDto.from(purchaseOrders);
        
        return ResponseEntity.ok(ApiResponse.success(response, "발주서 목록 조회에 성공했습니다.", HttpStatus.OK));
    }

    @GetMapping("/{purchaseOrderId}")
    public ResponseEntity<ApiResponse<PurchaseOrderDetailResponseDto>> getPurchaseOrderDetail(
            @PathVariable String purchaseOrderId) {
        
        PurchaseOrderDetailResponseDto detail = purchaseOrderService.getPurchaseOrderDetail(purchaseOrderId);
        
        return ResponseEntity.ok(ApiResponse.success(detail, "발주서 상세 정보 조회에 성공했습니다.", HttpStatus.OK));
    }

    /**
     * 발주서 승인
     */
    @PostMapping("/{purchaseOrderId}/approve")
    public ResponseEntity<ApiResponse<Void>> approvePurchaseOrder(@PathVariable String purchaseOrderId) {
        try {
            purchaseOrderService.approvePurchaseOrder(purchaseOrderId);
            return ResponseEntity.ok(ApiResponse.success(null, "발주서 승인이 완료되었습니다.", HttpStatus.OK));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("발주서 승인 실패: " + e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }

    /**
     * 발주서 반려
     */
    @PostMapping("/{purchaseOrderId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectPurchaseOrder(
            @PathVariable String purchaseOrderId,
            @RequestBody PurchaseOrderRejectRequestDto requestDto) {
        try {
            purchaseOrderService.rejectPurchaseOrder(purchaseOrderId, requestDto.getReason());
            return ResponseEntity.ok(ApiResponse.success(null, "발주서 반려가 완료되었습니다.", HttpStatus.OK));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("발주서 반려 실패: " + e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }
}
