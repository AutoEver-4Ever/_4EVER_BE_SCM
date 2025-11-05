package org.ever._4ever_be_scm.scm.pp.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.ever._4ever_be_scm.common.response.ApiResponse;
import org.ever._4ever_be_scm.scm.pp.dto.MesDetailResponseDto;
import org.ever._4ever_be_scm.scm.pp.dto.MesQueryResponseDto;
import org.ever._4ever_be_scm.scm.pp.service.MesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

@Tag(name = "MES 관리", description = "작업 지시 관리 API")
@RestController
@RequestMapping("/scm-pp/pp/mes")
@RequiredArgsConstructor
public class MesController {

    private final MesService mesService;

    /**
     * MES 목록 조회
     */
    @GetMapping
    @io.swagger.v3.oas.annotations.Operation(
            summary = "MES 목록 조회",
            description = "작업 지시(MES) 목록을 조회합니다. 견적ID와 상태로 필터링할 수 있습니다."
    )
    public ResponseEntity<ApiResponse<MesQueryResponseDto>> getMesList(
            @io.swagger.v3.oas.annotations.Parameter(description = "견적 ID (선택)")
            @RequestParam(required = false) String quotationId,
            @io.swagger.v3.oas.annotations.Parameter(description = "상태 (ALL, PENDING, IN_PROGRESS, COMPLETED)")
            @RequestParam(defaultValue = "ALL") String status,
            @io.swagger.v3.oas.annotations.Parameter(description = "페이지 번호")
            @RequestParam(defaultValue = "0") int page,
            @io.swagger.v3.oas.annotations.Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "20") int size) {

        MesQueryResponseDto result = mesService.getMesList(quotationId, status, page, size);

        return ResponseEntity.ok(ApiResponse.success(result, "성공적으로 조회했습니다.", HttpStatus.OK));
    }

    /**
     * MES 상세 조회
     */
    @GetMapping("/{mesId}")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "MES 상세 조회",
            description = "작업 지시(MES)의 상세 정보와 공정별 진행 상태를 조회합니다."
    )
    public ResponseEntity<ApiResponse<MesDetailResponseDto>> getMesDetail(
            @io.swagger.v3.oas.annotations.Parameter(description = "MES ID")
            @PathVariable String mesId) {

        MesDetailResponseDto result = mesService.getMesDetail(mesId);

        return ResponseEntity.ok(ApiResponse.success(result, "작업 지시 상세를 조회했습니다.", HttpStatus.OK));
    }

    /**
     * MES 시작
     */
    @PutMapping("/{mesId}/start")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "MES 시작",
            description = "PENDING 상태의 MES를 시작하고 자재를 소비합니다."
    )
    public DeferredResult<ResponseEntity<ApiResponse<Void>>> startMes(
            @io.swagger.v3.oas.annotations.Parameter(description = "MES ID")
            @PathVariable String mesId) {

        return mesService.startMesAsync(mesId);
    }

    /**
     * 공정 시작
     */
    @PutMapping("/{mesId}/operations/{operationId}/start")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "공정 시작",
            description = "특정 공정을 시작합니다. 이전 공정들이 모두 완료되어야 합니다."
    )
    public ResponseEntity<ApiResponse<Void>> startOperation(
            @io.swagger.v3.oas.annotations.Parameter(description = "MES ID")
            @PathVariable String mesId,
            @io.swagger.v3.oas.annotations.Parameter(description = "Operation ID")
            @PathVariable String operationId,
            @io.swagger.v3.oas.annotations.Parameter(description = "담당자 ID (선택)")
            @RequestParam(required = false) String managerId) {

        mesService.startOperation(mesId, operationId, managerId);

        return ResponseEntity.ok(ApiResponse.success(null, "공정이 시작되었습니다.", HttpStatus.OK));
    }

    /**
     * 공정 완료
     */
    @PutMapping("/{mesId}/operations/{operationId}/complete")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "공정 완료",
            description = "IN_PROGRESS 상태의 공정을 완료하고 진행률을 업데이트합니다."
    )
    public ResponseEntity<ApiResponse<Void>> completeOperation(
            @io.swagger.v3.oas.annotations.Parameter(description = "MES ID")
            @PathVariable String mesId,
            @io.swagger.v3.oas.annotations.Parameter(description = "Operation ID")
            @PathVariable String operationId) {

        mesService.completeOperation(mesId, operationId);

        return ResponseEntity.ok(ApiResponse.success(null, "공정이 완료되었습니다.", HttpStatus.OK));
    }

    /**
     * MES 완료
     */
    @PutMapping("/{mesId}/complete")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "MES 완료",
            description = "모든 공정이 완료된 MES를 완료 처리하고 완제품 재고를 증가시킵니다."
    )
    public DeferredResult<ResponseEntity<ApiResponse<Void>>> completeMes(
            @io.swagger.v3.oas.annotations.Parameter(description = "MES ID")
            @PathVariable String mesId) {

        return mesService.completeMesAsync(mesId);
    }
}
