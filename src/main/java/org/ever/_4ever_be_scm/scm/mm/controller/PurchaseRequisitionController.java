package org.ever._4ever_be_scm.scm.mm.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.ever._4ever_be_scm.common.response.ApiResponse;
import org.ever._4ever_be_scm.scm.iv.dto.PagedResponseDto;
import org.ever._4ever_be_scm.scm.mm.dto.*;
import org.ever._4ever_be_scm.scm.mm.service.PurchaseRequisitionService;
import org.ever._4ever_be_scm.scm.mm.vo.PurchaseRequisitionCreateVo;
import org.ever._4ever_be_scm.scm.mm.vo.PurchaseRequisitionSearchVo;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.stream.Collectors;

@Tag(name = "구매관리", description = "구매 관리 API")
@RestController
@RequestMapping("/scm-pp/mm/purchase-requisitions")
@RequiredArgsConstructor
public class PurchaseRequisitionController {
    
    private final PurchaseRequisitionService purchaseRequisitionService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponseDto<PurchaseRequisitionListResponseDto>>> getPurchaseRequisitionList(
            @RequestParam(defaultValue = "ALL") String statusCode,
            @io.swagger.v3.oas.annotations.Parameter(description = "검색 타입 (requesterName, departmentName, productRequestNumber)")
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        PurchaseRequisitionSearchVo searchVo = PurchaseRequisitionSearchVo.builder()
                .statusCode(statusCode)
                .type(type)
                .keyword(keyword)
                .startDate(startDate)
                .endDate(endDate)
                .page(page)
                .size(size)
                .build();
        
        Page<PurchaseRequisitionListResponseDto> purchaseRequisitions = purchaseRequisitionService.getPurchaseRequisitionList(searchVo);
        PagedResponseDto<PurchaseRequisitionListResponseDto> response = PagedResponseDto.from(purchaseRequisitions);
        
        return ResponseEntity.ok(ApiResponse.success(response, "구매요청서 목록입니다.", HttpStatus.OK));
    }

    @GetMapping("/{purchaseRequisitionId}")
    public ResponseEntity<ApiResponse<PurchaseRequisitionDetailResponseDto>> getPurchaseRequisitionDetail(
            @PathVariable String purchaseRequisitionId) {
        
        PurchaseRequisitionDetailResponseDto detail = purchaseRequisitionService.getPurchaseRequisitionDetail(purchaseRequisitionId);
        
        return ResponseEntity.ok(ApiResponse.success(detail, "구매요청서 상세입니다.", HttpStatus.OK));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createPurchaseRequisition(
            @RequestBody PurchaseRequisitionCreateRequestDto requestDto) {
        
        // DTO to VO 변환
        PurchaseRequisitionCreateVo createVo = PurchaseRequisitionCreateVo.builder()
                .requesterId(requestDto.getRequesterId())
                .requesterId(requestDto.getRequesterId())
                .items(requestDto.getItems().stream()
                        .map(item -> PurchaseRequisitionCreateVo.ItemVo.builder()
                                .itemName(item.getItemName())
                                .quantity(item.getQuantity())
                                .uomName(item.getUomName())
                                .expectedUnitPrice(item.getExpectedUnitPrice())
                                .preferredSupplierName(item.getPreferredSupplierName())
                                .dueDate(item.getDueDate())
                                .purpose(item.getPurpose())
                                .note(item.getNote())
                                .build())
                        .collect(Collectors.toList()))
                .build();
        
        purchaseRequisitionService.createPurchaseRequisition(createVo);
        
        return ResponseEntity.ok(ApiResponse.success(null, "비재고성 구매요청서가 생성되었습니다.", HttpStatus.OK));
    }

    @PostMapping("/{purchaseRequisitionId}/approve")
    public ResponseEntity<ApiResponse<Void>> approvePurchaseRequisition(
            @PathVariable String purchaseRequisitionId) {
        
        purchaseRequisitionService.approvePurchaseRequisition(purchaseRequisitionId);
        
        return ResponseEntity.ok(ApiResponse.success(null, "구매요청서가 승인되었습니다.", HttpStatus.OK));
    }

    @PostMapping("/{purchaseRequisitionId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectPurchaseRequisition(
            @PathVariable String purchaseRequisitionId,
            @RequestBody PurchaseRequisitionRejectRequestDto requestDto) {
        
        purchaseRequisitionService.rejectPurchaseRequisition(purchaseRequisitionId, requestDto);
        
        return ResponseEntity.ok(ApiResponse.success(null, "구매요청서가 반려되었습니다.", HttpStatus.OK));
    }
}
