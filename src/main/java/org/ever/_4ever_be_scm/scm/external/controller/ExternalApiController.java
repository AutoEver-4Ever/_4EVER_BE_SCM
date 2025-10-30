package org.ever._4ever_be_scm.scm.external.controller;

import lombok.RequiredArgsConstructor;
import org.ever._4ever_be_scm.common.response.ApiResponse;
import org.ever._4ever_be_scm.scm.external.dto.*;
import org.ever._4ever_be_scm.scm.external.service.ExternalApiService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/scm-pp")
@RequiredArgsConstructor
public class ExternalApiController {

    private final ExternalApiService externalApiService;

    /**
     * 1. ProductOrderId로 ProductOrderItem들 가져오기
     */
    @PostMapping("/product/orderItem")
    public ResponseEntity<ApiResponse<ProductOrderItemResponseDto>> getProductOrderItems(
            @RequestBody ProductOrderRequestDto request) {
        try {
            ProductOrderItemResponseDto response = externalApiService.getProductOrderItems(request.getProductOrderId());
            return ResponseEntity.ok(ApiResponse.success(response, "성공 메시지", HttpStatus.OK));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("조회 실패: " + e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }

    /**
     * 2. ProductOrderId들로 ProductOrder 정보 제공
     */
    @PostMapping("/product/orderInfos")
    public ResponseEntity<ApiResponse<List<ProductOrderInfoDto>>> getProductOrderInfos(
            @RequestBody ProductOrderIdsRequestDto request) {
        try {
            List<ProductOrderInfoDto> response = externalApiService.getProductOrderInfos(request.getProductOrderIds());
            return ResponseEntity.ok(ApiResponse.success(response, "성공 메시지", HttpStatus.OK));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("조회 실패: " + e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }

    /**
     * 3. ProductId들로 Product 정보들 제공
     */
    @PostMapping("/product/multiple")
    public ResponseEntity<ApiResponse<ProductMultipleResponseDto>> getProductsMultiple(
            @RequestBody ProductMultipleRequestDto request) {
        try {
            ProductMultipleResponseDto response = externalApiService.getProductsMultiple(request.getProductIds());
            return ResponseEntity.ok(ApiResponse.success(response, "성공 메시지", HttpStatus.OK));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("조회 실패: " + e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }

    /**
     * 4. ItemId를 통해 부족재고 파악
     */
    @PostMapping("/inventory/stock-check")
    public ResponseEntity<ApiResponse<StockCheckResponseDto>> checkStock(
            @RequestBody StockCheckRequestDto request) {
        try {
            StockCheckResponseDto response = externalApiService.checkStock(request.getItems());
            return ResponseEntity.ok(ApiResponse.success(response, "재고 확인을 완료했습니다.", HttpStatus.OK));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("재고 확인 실패: " + e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }

    /**
     * 5. SupplierCompanyId로 SupplierCompany 정보 가져오기(단건)
     */
    @PostMapping("/company/supplier/single")
    public ResponseEntity<ApiResponse<SupplierCompanySingleResponseDto>> getSupplierCompanySingle(
            @RequestBody SupplierCompanySingleRequestDto request) {
        try {
            SupplierCompanySingleResponseDto response = externalApiService.getSupplierCompanySingle(request.getSupplierCompanyId());
            return ResponseEntity.ok(ApiResponse.success(response, "성공 메시지", HttpStatus.OK));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("조회 실패: " + e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }

    /**
     * 6. SupplierCompanyId들로 SupplierCompany 정보 가져오기(다수)
     */
    @PostMapping("/company/supplier/multiple")
    public ResponseEntity<ApiResponse<SupplierCompanyMultipleResponseDto>> getSupplierCompaniesMultiple(
            @RequestBody SupplierCompanyMultipleRequestDto request) {
        try {
            SupplierCompanyMultipleResponseDto response = externalApiService.getSupplierCompaniesMultiple(request.getSupplierCompanyIds());
            return ResponseEntity.ok(ApiResponse.success(response, "성공 메시지", HttpStatus.OK));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("조회 실패: " + e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }

    /**
     * 7. 카테고리가 ITEM인 Product 목록 반환
     */
    @GetMapping("/product/item/toggle")
    public ResponseEntity<ApiResponse<ProductMultipleResponseDto>> getItemCategoryProducts() {
        try {
            ProductMultipleResponseDto response = externalApiService.getItemCategoryProducts();
            return ResponseEntity.ok(ApiResponse.success(response, "성공 메시지", HttpStatus.OK));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("조회 실패: " + e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }
}
