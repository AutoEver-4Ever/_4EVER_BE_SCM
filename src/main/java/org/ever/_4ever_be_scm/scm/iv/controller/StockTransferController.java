package org.ever._4ever_be_scm.scm.iv.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.ever._4ever_be_scm.common.response.ApiResponse;
import org.ever._4ever_be_scm.scm.iv.dto.PagedResponseDto;
import org.ever._4ever_be_scm.scm.iv.dto.StockDeliveryRequestDto;
import org.ever._4ever_be_scm.scm.iv.dto.StockTransferDto;
import org.ever._4ever_be_scm.scm.iv.dto.StockTransferRequestDto;
import org.ever._4ever_be_scm.scm.iv.service.StockTransferService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 재고 이동 관리 컨트롤러
 */
@Tag(name = "재고관리", description = "재고 관리 API")
@RestController
@RequestMapping("/scm-pp/iv")
@RequiredArgsConstructor
public class StockTransferController {

    private final StockTransferService stockTransferService;
    
    /**
     * 재고 이동 목록 조회 API
     *
     * @return 재고 이동 목록
     */
    @GetMapping("/stock-transfers")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "재고 이동 목록 조회 (상위 5개)"
    )
    public ResponseEntity<ApiResponse<PagedResponseDto<StockTransferDto>>> getStockTransfers() {
        PageRequest pageable = PageRequest.of(0, 5); // 항상 첫 페이지에서 5개만 조회
        Page<StockTransferDto> stockTransfers = stockTransferService.getStockTransfers(pageable);
        PagedResponseDto<StockTransferDto> response = PagedResponseDto.from(stockTransfers);
        return ResponseEntity.ok(ApiResponse.success(response, "상위 5개의 재고 이력 목록을 조회했습니다.", HttpStatus.OK));
    }
    
    /**
     * 창고간 재고 이동 생성 API
     *
     * @param request 재고 이동 요청 정보
     * @return 재고 이동 결과
     */
    @PostMapping("/stock-transfers")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "창고간 재고 이동"
    )
    public ResponseEntity<ApiResponse<String>> createStockTransfer(
            @RequestBody StockTransferRequestDto request,
            @RequestParam String requesterId
    ) {
        try {
            stockTransferService.createStockTransfer(request,requesterId);
            return ResponseEntity.ok(ApiResponse.success(null, "재고 이동이 성공적으로 처리되었습니다.", HttpStatus.OK));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("재고 이동 처리 실패: " + e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }
    
    /**
     * 재고 입출고 처리 API
     *
     * @param request 재고 입출고 요청 정보
     * @return 재고 입출고 결과
     */
    @PostMapping("/stock-transfers/deliver")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "재고 입출고 처리"
    )
    public ResponseEntity<ApiResponse<String>> processStockDelivery(@RequestBody StockDeliveryRequestDto request) {
        try {
//            stockTransferService.processStockDelivery(request);
            return ResponseEntity.ok(ApiResponse.success(null, "재고 입출고가 성공적으로 처리되었습니다.", HttpStatus.OK));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("재고 입출고 처리 실패: " + e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }
}
