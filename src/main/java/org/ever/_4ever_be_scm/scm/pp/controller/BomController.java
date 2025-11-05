package org.ever._4ever_be_scm.scm.pp.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.ever._4ever_be_scm.common.response.ApiResponse;
import org.ever._4ever_be_scm.scm.iv.dto.PagedResponseDto;
import org.ever._4ever_be_scm.scm.pp.dto.BomDetailResponseDto;
import org.ever._4ever_be_scm.scm.pp.dto.BomListResponseDto;
import org.ever._4ever_be_scm.scm.pp.dto.BomCreateRequestDto;
import org.ever._4ever_be_scm.scm.pp.dto.ProductMapResponseDto;
import org.ever._4ever_be_scm.scm.pp.dto.ProductDetailResponseDto;
import org.ever._4ever_be_scm.scm.pp.service.BomService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "생산관리", description = "생산 관리 API")
@RestController
@RequestMapping("/scm-pp/pp/boms")
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

    /**
     * Product ID와 이름 맵 조회 (전체 목록)
     */
    @GetMapping("/products")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Product ID-이름 맵 조회",
            description = "Product의 ID를 key, productName을 value로 하는 맵 형태로 전체 목록을 조회합니다."
    )
    public ResponseEntity<ApiResponse<List<ProductMapResponseDto>>> getProductMap() {

        List<ProductMapResponseDto> productMapList = bomService.getProductMap();

        return ResponseEntity.ok(ApiResponse.success(productMapList, "Product 맵 조회 성공", HttpStatus.OK));
    }

    /**
     * Product 상세 정보 조회
     */
    @GetMapping("/products/{productId}")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Product 상세 정보 조회",
            description = "Product의 상세 정보(id, 이름, 타입, 제품코드, 단위, 단가, 공급업체명)를 조회합니다."
    )
    public ResponseEntity<ApiResponse<ProductDetailResponseDto>> getProductDetail(
            @io.swagger.v3.oas.annotations.Parameter(description = "Product ID")
            @PathVariable String productId) {

        ProductDetailResponseDto detail = bomService.getProductDetail(productId);

        return ResponseEntity.ok(ApiResponse.success(detail, "Product 상세 조회 성공", HttpStatus.OK));
    }

    /**
     * Operation ID와 이름 맵 조회 (전체 목록)
     */
    @GetMapping("/operations")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Operation ID-이름 맵 조회",
            description = "Operation의 ID를 key, opName을 value로 하는 맵 형태로 전체 목록을 조회합니다."
    )
    public ResponseEntity<ApiResponse<List<ProductMapResponseDto>>> getOperationMap() {

        List<ProductMapResponseDto> operationMapList = bomService.getOperationMap();

        return ResponseEntity.ok(ApiResponse.success(operationMapList, "Operation 맵 조회 성공", HttpStatus.OK));
    }

}
