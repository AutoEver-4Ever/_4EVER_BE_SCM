package org.ever._4ever_be_scm.scm.mm.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.ever._4ever_be_scm.common.response.ApiResponse;
import org.ever._4ever_be_scm.scm.mm.dto.StockPurchaseRequestDto;
import org.ever._4ever_be_scm.scm.mm.service.StockPurchaseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "구매관리", description = "구매 관리 API")
@RestController
@RequestMapping("/scm-pp/mm/stock-purchase-requisitions")
@RequiredArgsConstructor
public class StockPurchaseController {

    private final StockPurchaseService stockPurchaseService;

    /**
     * 재고성 구매요청 생성
     */
    @PostMapping
    public ResponseEntity<ApiResponse<String>> createStockPurchaseRequest(
            @RequestBody StockPurchaseRequestDto requestDto,
            @RequestParam String requesterId
            ) {
        try {
            String requestId = stockPurchaseService.createStockPurchaseRequest(requestDto, requesterId);
            return ResponseEntity.ok(ApiResponse.success(requestId, "재고성 구매요청이 생성되었습니다.", HttpStatus.OK));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("재고성 구매요청 생성 실패: " + e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }
}
