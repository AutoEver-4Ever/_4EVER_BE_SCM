package org.ever._4ever_be_scm.scm.controller;

import lombok.RequiredArgsConstructor;
import org.ever._4ever_be_scm.common.response.ApiResponse;
import org.ever._4ever_be_scm.scm.dto.PagedResponseDto;
import org.ever._4ever_be_scm.scm.dto.StockTransferDetailDto;
import org.ever._4ever_be_scm.scm.dto.StockTransferDto;
import org.ever._4ever_be_scm.scm.service.StockTransferService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 재고 이동 관리 컨트롤러
 */
@RestController
@RequestMapping("/api/scm-pp/iv")
@RequiredArgsConstructor
public class StockTransferController {

    private final StockTransferService stockTransferService;
    
    /**
     * 재고 이동 목록 조회 API
     *
     * @return 재고 이동 목록
     */
    @GetMapping("/stock-transfers")
    public ResponseEntity<ApiResponse<PagedResponseDto<StockTransferDto>>> getStockTransfers() {
        PageRequest pageable = PageRequest.of(0, 5); // 항상 첫 페이지에서 5개만 조회
        Page<StockTransferDto> stockTransfers = stockTransferService.getStockTransfers(pageable);
        PagedResponseDto<StockTransferDto> response = PagedResponseDto.from(stockTransfers);
        return ResponseEntity.ok(ApiResponse.success(response, "상위 5개의 재고 이력 목록을 조회했습니다.", HttpStatus.OK));
    }

    /**
     * 재고 이동 상세 목록 조회 API
     * 
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 재고 이동 상세 목록
     */
    @GetMapping("/stock-transfers/detail")
    public ResponseEntity<ApiResponse<PagedResponseDto<StockTransferDetailDto>>> getStockTransfersDetail(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<StockTransferDetailDto> stockTransfersDetail = stockTransferService.getStockTransfersDetail(PageRequest.of(page, size));
        PagedResponseDto<StockTransferDetailDto> response = PagedResponseDto.from(stockTransfersDetail);
        return ResponseEntity.ok(ApiResponse.success(response, "재고 이력 목록을 조회했습니다.", HttpStatus.OK));
    }
}
