package org.ever._4ever_be_scm.scm.pp.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.ever._4ever_be_scm.common.response.ApiResponse;
import org.ever._4ever_be_scm.scm.iv.dto.PagedResponseDto;
import org.ever._4ever_be_scm.scm.pp.dto.BomDetailResponseDto;
import org.ever._4ever_be_scm.scm.pp.dto.BomListResponseDto;
import org.ever._4ever_be_scm.scm.pp.dto.BomCreateRequestDto;
import org.ever._4ever_be_scm.scm.pp.service.BomService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "생산관리", description = "생산 관리 API")
@RestController
@RequestMapping("/api/scm-pp/pp/boms")
@RequiredArgsConstructor
public class BomController {
    private final BomService bomService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createBom(@RequestBody BomCreateRequestDto requestDto) {
        bomService.createBom(requestDto);
        return ResponseEntity.ok(ApiResponse.success(null, "BOM 생성 성공", HttpStatus.OK));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponseDto<BomListResponseDto>>> getBomList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<BomListResponseDto> bomList = bomService.getBomList(PageRequest.of(page, size));
        PagedResponseDto<BomListResponseDto> response = PagedResponseDto.from(bomList);

        return ResponseEntity.ok(ApiResponse.success(response, "BOM 목록 조회 성공", HttpStatus.OK));
    }

    @GetMapping("/{bomId}")
    public ResponseEntity<ApiResponse<BomDetailResponseDto>> getBomDetail(@PathVariable String bomId) {
        BomDetailResponseDto detail = bomService.getBomDetail(bomId);
        return ResponseEntity.ok(ApiResponse.success(detail, "BOM 상세 조회 성공", HttpStatus.OK));
    }

    @PatchMapping("/{bomId}")
    public ResponseEntity<ApiResponse<Void>> updateBom(@PathVariable String bomId, @RequestBody BomCreateRequestDto requestDto) {
        bomService.updateBom(bomId, requestDto);
        return ResponseEntity.ok(ApiResponse.success(null, "BOM 수정 성공", HttpStatus.OK));
    }

}
