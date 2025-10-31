package org.ever._4ever_be_scm.scm.pp.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.ever._4ever_be_scm.common.response.ApiResponse;
import org.ever._4ever_be_scm.scm.iv.dto.PagedResponseDto;
import org.ever._4ever_be_scm.scm.pp.dto.*;
import org.ever._4ever_be_scm.scm.pp.service.QuotationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "생산관리", description = "생산 관리 API")
@RestController
@RequestMapping("/scm-pp/pp/quotations")
@RequiredArgsConstructor
public class QuotationController {
    
    private final QuotationService quotationService;

    @PostMapping("/simulate")
    public ResponseEntity<ApiResponse<PagedResponseDto<QuotationSimulateResponseDto>>> simulateQuotations(
            @RequestBody QuotationSimulateRequestDto requestDto,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<QuotationSimulateResponseDto> simulationResults = quotationService.simulateQuotations(
            requestDto, PageRequest.of(page, size));
        PagedResponseDto<QuotationSimulateResponseDto> response = PagedResponseDto.from(simulationResults);
        
        return ResponseEntity.ok(ApiResponse.success(response, "견적 시뮬레이션 완료", HttpStatus.OK));
    }

    @PostMapping("/preview")
    public ResponseEntity<ApiResponse<List<MpsPreviewResponseDto>>> previewMps(
            @RequestBody List<String> quotationIds) {
        
        List<MpsPreviewResponseDto> mpsPreview = quotationService.previewMps(quotationIds);
        
        return ResponseEntity.ok(ApiResponse.success(mpsPreview, "MPS 프리뷰 생성 완료", HttpStatus.OK));
    }

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmQuotations(
            @RequestBody QuotationConfirmRequestDto requestDto) {
        
        quotationService.confirmQuotations(requestDto);
        
        return ResponseEntity.ok(ApiResponse.success(null, "견적 확정 완료", HttpStatus.OK));
    }
}
