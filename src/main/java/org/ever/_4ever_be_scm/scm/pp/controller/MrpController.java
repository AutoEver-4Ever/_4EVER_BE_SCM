package org.ever._4ever_be_scm.scm.pp.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.ever._4ever_be_scm.common.response.ApiResponse;
import org.ever._4ever_be_scm.scm.pp.dto.MrpRunConvertRequestDto;
import org.ever._4ever_be_scm.scm.pp.dto.MrpRunQueryResponseDto;
import org.ever._4ever_be_scm.scm.pp.service.MrpService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "MRP 관리", description = "MRP 계획주문 관리 API")
@RestController
@RequestMapping("/scm-pp/pp/mrp")
@RequiredArgsConstructor
public class MrpController {

    private final MrpService mrpService;

    /**
     * MRP → MRP_RUN 계획주문 전환
     */
    @PostMapping("/convert")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "계획주문 전환",
            description = "선택한 MRP 자재들을 계획주문(MRP_RUN)으로 전환합니다."
    )
    public ResponseEntity<ApiResponse<Void>> convertToMrpRun(
            @RequestBody MrpRunConvertRequestDto requestDto) {

        mrpService.convertToMrpRun(requestDto);

        return ResponseEntity.ok(ApiResponse.success(null, "계획주문 전환 완료", HttpStatus.OK));
    }

    /**
     * MRP 계획주문 목록 조회
     */
    @GetMapping("/runs")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "계획주문 목록 조회",
            description = "MRP 계획주문 목록을 조회합니다. 상태별 필터링이 가능합니다."
    )
    public ResponseEntity<ApiResponse<MrpRunQueryResponseDto>> getMrpRunList(
            @io.swagger.v3.oas.annotations.Parameter(description = "상태 (ALL, PENDING, APPROVAL, REJECTED, COMPLETED)")
            @RequestParam(defaultValue = "ALL") String status,
            @io.swagger.v3.oas.annotations.Parameter(description = "페이지 번호")
            @RequestParam(defaultValue = "0") int page,
            @io.swagger.v3.oas.annotations.Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "10") int size) {

        MrpRunQueryResponseDto result = mrpService.getMrpRunList(status, page, size);

        return ResponseEntity.ok(ApiResponse.success(result, "계획주문 목록을 조회했습니다.", HttpStatus.OK));
    }

    /**
     * MRP 계획주문 승인
     */
    @PutMapping("/runs/{mrpRunId}/approve")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "계획주문 승인",
            description = "PENDING 상태의 계획주문을 승인하여 발주를 진행합니다."
    )
    public ResponseEntity<ApiResponse<Void>> approveMrpRun(
            @io.swagger.v3.oas.annotations.Parameter(description = "MRP_RUN ID")
            @PathVariable String mrpRunId) {

        mrpService.approveMrpRun(mrpRunId);

        return ResponseEntity.ok(ApiResponse.success(null, "계획주문이 승인되었습니다.", HttpStatus.OK));
    }

    /**
     * MRP 계획주문 거부
     */
    @PutMapping("/runs/{mrpRunId}/reject")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "계획주문 거부",
            description = "PENDING 상태의 계획주문을 거부합니다."
    )
    public ResponseEntity<ApiResponse<Void>> rejectMrpRun(
            @io.swagger.v3.oas.annotations.Parameter(description = "MRP_RUN ID")
            @PathVariable String mrpRunId) {

        mrpService.rejectMrpRun(mrpRunId);

        return ResponseEntity.ok(ApiResponse.success(null, "계획주문이 거부되었습니다.", HttpStatus.OK));
    }

    /**
     * MRP 계획주문 입고 처리
     */
    @PutMapping("/runs/{mrpRunId}/receive")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "계획주문 입고",
            description = "APPROVAL 상태의 계획주문을 입고 처리하고 재고를 자동으로 증가시킵니다."
    )
    public ResponseEntity<ApiResponse<Void>> receiveMrpRun(
            @io.swagger.v3.oas.annotations.Parameter(description = "MRP_RUN ID")
            @PathVariable String mrpRunId) {

        mrpService.receiveMrpRun(mrpRunId);

        return ResponseEntity.ok(ApiResponse.success(null, "입고가 완료되었습니다.", HttpStatus.OK));
    }
}
