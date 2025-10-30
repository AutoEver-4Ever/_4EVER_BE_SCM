package org.ever._4ever_be_scm.scm.mm.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.ever._4ever_be_scm.common.response.ApiResponse;
import org.ever._4ever_be_scm.scm.iv.dto.PagedResponseDto;
import org.ever._4ever_be_scm.scm.mm.dto.SupplierDetailResponseDto;
import org.ever._4ever_be_scm.scm.mm.dto.SupplierListResponseDto;
import org.ever._4ever_be_scm.scm.mm.dto.supplier.SupplierCreateRequestDto;
import org.ever._4ever_be_scm.scm.mm.service.SupplierService;
import org.ever._4ever_be_scm.scm.mm.vo.SupplierSearchVo;
import org.ever.event.CreateAuthUserResultEvent;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

@Tag(name = "구매관리", description = "구매 관리 API")
@RestController
@RequestMapping("/scm-pp/mm/supplier")
@RequiredArgsConstructor
public class SupplierController {
    
    private final SupplierService supplierService;

    @GetMapping("/supplier")
    public ResponseEntity<ApiResponse<PagedResponseDto<SupplierListResponseDto>>> getSupplierList(
            @RequestParam(defaultValue = "ALL") String statusCode,
            @RequestParam(defaultValue = "ALL") String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        SupplierSearchVo searchVo = SupplierSearchVo.builder()
                .statusCode(statusCode)
                .category(category)
                .page(page)
                .size(size)
                .build();
        
        Page<SupplierListResponseDto> suppliers = supplierService.getSupplierList(searchVo);
        PagedResponseDto<SupplierListResponseDto> response = PagedResponseDto.from(suppliers);
        
        return ResponseEntity.ok(ApiResponse.success(response, "공급업체 목록을 조회했습니다.", HttpStatus.OK));
    }

    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<ApiResponse<SupplierDetailResponseDto>> getSupplierDetail(
            @PathVariable String supplierId) {
        
        SupplierDetailResponseDto detail = supplierService.getSupplierDetail(supplierId);
        
        return ResponseEntity.ok(ApiResponse.success(detail, "공급업체 상세 정보를 조회했습니다.", HttpStatus.OK));
    }

    @PostMapping()
    public DeferredResult<ResponseEntity<ApiResponse<CreateAuthUserResultEvent>>> createSupplier(
        @RequestBody SupplierCreateRequestDto requestDto
    ) {
        DeferredResult<ResponseEntity<ApiResponse<CreateAuthUserResultEvent>>> deferredResult = new DeferredResult<>(30000L);
        deferredResult.onTimeout(() -> deferredResult.setResult(
            ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT)
                .body(ApiResponse.fail("[SAGA][FAIL] 처리 시간이 초과되었습니다.", HttpStatus.REQUEST_TIMEOUT))
        ));

        supplierService.createSupplier(requestDto, deferredResult);
        return deferredResult;
    }
}
