package org.ever._4ever_be_scm.scm.pp.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.ever._4ever_be_scm.common.response.ApiResponse;
import org.ever._4ever_be_scm.scm.iv.dto.PagedResponseDto;
import org.ever._4ever_be_scm.scm.pp.dto.*;
import org.ever._4ever_be_scm.scm.pp.service.QuotationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "생산관리", description = "생산 관리 API")
@RestController
@RequestMapping("/scm-pp/pp/quotations")
@RequiredArgsConstructor
public class QuotationController {
    
    private final QuotationService quotationService;

    /**
     * 견적 목록 조회 (그룹핑된 형태)
     */
    @GetMapping
    @io.swagger.v3.oas.annotations.Operation(
            summary = "견적 목록 조회",
            description = "견적 목록을 그룹핑하여 조회합니다. 같은 견적번호의 여러 품목은 하나로 그룹핑됩니다."
    )
    public ResponseEntity<ApiResponse<QuotationGroupListResponseDto>> getQuotationList(
            @io.swagger.v3.oas.annotations.Parameter(description = "상태코드 (ALL, REVIEW, APPROVAL)")
            @RequestParam(defaultValue = "ALL") String statusCode,
            @io.swagger.v3.oas.annotations.Parameter(description = "시작 날짜 (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @io.swagger.v3.oas.annotations.Parameter(description = "종료 날짜 (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @io.swagger.v3.oas.annotations.Parameter(description = "페이지 번호")
            @RequestParam(defaultValue = "0") int page,
            @io.swagger.v3.oas.annotations.Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "10") int size) {
        
        QuotationGroupListResponseDto result = quotationService.getQuotationList(
                statusCode, startDate, endDate, page, size);
        
        return ResponseEntity.ok(ApiResponse.success(result, "견적 목록을 조회했습니다.", HttpStatus.OK));
    }

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

    /**
     * MPS 조회 (주차별)
     */
    @GetMapping("/mps")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "MPS 조회",
            description = "BOM ID를 기준으로 주차별 생산 계획(MPS)을 조회합니다. startDate 앞 3주차부터 조회되며 최소 7주차를 보장합니다."
    )
    public ResponseEntity<ApiResponse<MpsQueryResponseDto>> getMps(
            @io.swagger.v3.oas.annotations.Parameter(description = "BOM ID")
            @RequestParam String bomId,
            @io.swagger.v3.oas.annotations.Parameter(description = "시작 날짜 (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @io.swagger.v3.oas.annotations.Parameter(description = "종료 날짜 (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @io.swagger.v3.oas.annotations.Parameter(description = "페이지 번호")
            @RequestParam(defaultValue = "0") int page,
            @io.swagger.v3.oas.annotations.Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "7") int size) {

        MpsQueryResponseDto mpsData = quotationService.getMps(bomId, startDate, endDate, page, size);

        return ResponseEntity.ok(ApiResponse.success(mpsData, "MPS를 조회했습니다.", HttpStatus.OK));
    }

    /**
     * MRP 조회 (자재 조달 계획)
     */
    @GetMapping("/mrp")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "MRP 조회",
            description = "자재 소요 계획(MRP)을 조회합니다. 원자재별로 그룹핑되어 필요량이 합산되며, bomId 또는 quotationId로 필터링할 수 있습니다."
    )
    public ResponseEntity<ApiResponse<MrpQueryResponseDto>> getMrp(
            @io.swagger.v3.oas.annotations.Parameter(description = "BOM ID (선택)")
            @RequestParam(required = false) String bomId,
            @io.swagger.v3.oas.annotations.Parameter(description = "견적 ID (선택)")
            @RequestParam(required = false) String quotationId,
            @io.swagger.v3.oas.annotations.Parameter(description = "재고 상태 (ALL, SUFFICIENT, INSUFFICIENT)")
            @RequestParam(required = false, defaultValue = "ALL") String availableStatusCode,
            @io.swagger.v3.oas.annotations.Parameter(description = "페이지 번호")
            @RequestParam(defaultValue = "0") int page,
            @io.swagger.v3.oas.annotations.Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "10") int size) {

        MrpQueryResponseDto mrpData = quotationService.getMrp(bomId, quotationId, availableStatusCode, page, size);

        return ResponseEntity.ok(ApiResponse.success(mrpData, "자재 조달 계획을 조회했습니다.", HttpStatus.OK));
    }
}
