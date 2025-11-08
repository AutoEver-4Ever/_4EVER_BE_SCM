package org.ever._4ever_be_scm.scm.pp.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.ever._4ever_be_scm.common.response.ApiResponse;
import org.ever._4ever_be_scm.scm.mm.dto.ToggleCodeLabelDto;
import org.ever._4ever_be_scm.scm.pp.dto.MrpRunConvertRequestDto;
import org.ever._4ever_be_scm.scm.pp.dto.MrpRunQueryResponseDto;
import org.ever._4ever_be_scm.scm.pp.service.MrpService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
            @io.swagger.v3.oas.annotations.Parameter(description = "상태 (ALL, INITIAL, PENDING, REQUEST_APPROVED, ORDER_APPROVED, DELIVERING, DELIVERED)")
            @RequestParam(defaultValue = "ALL") String status,
            @io.swagger.v3.oas.annotations.Parameter(description = "견적 ID")
            @RequestParam(required = false) String quotationId,
            @io.swagger.v3.oas.annotations.Parameter(description = "페이지 번호")
            @RequestParam(defaultValue = "0") int page,
            @io.swagger.v3.oas.annotations.Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "10") int size) {

        MrpRunQueryResponseDto result = mrpService.getMrpRunList(status, quotationId, page, size);

        return ResponseEntity.ok(ApiResponse.success(result, "계획주문 목록을 조회했습니다.", HttpStatus.OK));
    }

    @GetMapping("runs/status/toggle")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "MRP Run 상태 목록 조회",
            description = "MRP Run 상태 필터용 토글 목록을 조회합니다."
    )
    public ResponseEntity<ApiResponse<List<ToggleCodeLabelDto>>> getMrpAvailableStatusToggle() {
        List<ToggleCodeLabelDto> list = List.of(
                new ToggleCodeLabelDto("전체 상태", "ALL"),
                new ToggleCodeLabelDto("신청전", "INITIAL"),
                new ToggleCodeLabelDto("반려", "REJECTED"),
                new ToggleCodeLabelDto("대기중", "PENDING"),
                new ToggleCodeLabelDto("요청 승인", "REQUEST_APPROVED"),
                new ToggleCodeLabelDto("발주서 승인", "ORDER_APPROVED"),
                new ToggleCodeLabelDto("배송중", "DELIVERING"),
                new ToggleCodeLabelDto("배송완료", "DELIVERED")
        );
        return ResponseEntity.ok(ApiResponse.success(list, "상태 목록 조회 성공", HttpStatus.OK));
    }

    @GetMapping("/runs/quotations/toggle")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "MRP Run 견적 목록 조회",
            description = "MRP Run 테이블에 존재하는 견적 목록을 조회합니다. quotationId를 value로, quotationNumber를 label로 반환합니다."
    )
    public ResponseEntity<ApiResponse<List<ToggleCodeLabelDto>>> getMrpRunQuotationList() {
        List<ToggleCodeLabelDto> result = mrpService.getMrpRunQuotationList();
        return ResponseEntity.ok(ApiResponse.success(result, "MRP Run 견적 목록을 조회했습니다.", HttpStatus.OK));
    }
}
