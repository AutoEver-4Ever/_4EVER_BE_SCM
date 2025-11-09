package org.ever._4ever_be_scm.scm.mm.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.ever._4ever_be_scm.common.response.ApiResponse;
import org.ever._4ever_be_scm.scm.mm.dto.MMStatisticsResponseDto;
import org.ever._4ever_be_scm.scm.mm.dto.SupplierOrderStatisticsResponseDto;
import org.ever._4ever_be_scm.scm.mm.dto.ToggleCodeLabelDto;
import java.util.List;
import org.ever._4ever_be_scm.scm.mm.service.MMStatisticsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "구매관리", description = "구매 관리 API")
@RestController
@RequestMapping("/scm-pp/mm")
@RequiredArgsConstructor
public class MMStatisticsController {

    private final MMStatisticsService mmStatisticsService;

    /**
     * MM 통계 조회
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<MMStatisticsResponseDto>> getMMStatistics() {
        try {
            MMStatisticsResponseDto statistics = mmStatisticsService.getMMStatistics();
            return ResponseEntity.ok(ApiResponse.success(statistics, "OK", HttpStatus.OK));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("통계 조회 실패: " + e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }

    @GetMapping("/purchase-requisition/status/toggle")
    public ApiResponse<List<ToggleCodeLabelDto>> getPurchaseRequisitionStatusToggle() {
        List<ToggleCodeLabelDto> list = List.of(
            new ToggleCodeLabelDto("전체 상태", "ALL"),
            new ToggleCodeLabelDto("승인", "APPROVAL"),
            new ToggleCodeLabelDto("대기", "PENDING"),
            new ToggleCodeLabelDto("반려", "REJECTED")
        );
    return ApiResponse.success(list, "상태 목록 조회 성공", org.springframework.http.HttpStatus.OK);
    }

    @GetMapping("/purchase-requisition/search-type/toggle")
    public ApiResponse<List<ToggleCodeLabelDto>> getPurchaseRequisitionSearchTypeToggle() {
        List<ToggleCodeLabelDto> list = List.of(
                new ToggleCodeLabelDto("요청자 이름", "requesterName"),
                new ToggleCodeLabelDto("부서명", "departmentName"),
                new ToggleCodeLabelDto("구매요청서 번호","productRequestNumber")
        );
        return ApiResponse.success(list, "카테고리 목록 조회 성공", org.springframework.http.HttpStatus.OK);
    }

    @GetMapping("/purchase-orders/status/toggle")
    public ApiResponse<List<ToggleCodeLabelDto>> getPurchaseOrderStatusToggle() {
        List<ToggleCodeLabelDto> list = List.of(
            new ToggleCodeLabelDto("전체 상태", "ALL"),
            new ToggleCodeLabelDto("승인", "APPROVAL"),
            new ToggleCodeLabelDto("대기", "PENDING"),
            new ToggleCodeLabelDto("반려", "REJECTED"),
            new ToggleCodeLabelDto("배송중", "DELIVERING"),
            new ToggleCodeLabelDto("배송완료", "DELIVERED")
        );
    return ApiResponse.success(list, "상태 목록 조회 성공", org.springframework.http.HttpStatus.OK);
    }

    @GetMapping("/purchase-orders/search-type/toggle")
    public ApiResponse<List<ToggleCodeLabelDto>> getPurchaseOrderSearchTypeToggle() {
        List<ToggleCodeLabelDto> list = List.of(
                new ToggleCodeLabelDto("공급 업체명", "SupplierCompanyNumber"),
                new ToggleCodeLabelDto("발주서 번호", "PurchaseOrderNumber")
        );
        return ApiResponse.success(list, "카테고리 목록 조회 성공", org.springframework.http.HttpStatus.OK);
    }

    @GetMapping("/supplier/status/toggle")
    public ApiResponse<List<ToggleCodeLabelDto>> getSupplierStatusToggle() {
        List<ToggleCodeLabelDto> list = List.of(
            new ToggleCodeLabelDto("전체 상태", "ALL"),
            new ToggleCodeLabelDto("활성", "ACTIVE"),
            new ToggleCodeLabelDto("비활성", "INACTIVE")
        );
    return ApiResponse.success(list, "상태 목록 조회 성공", org.springframework.http.HttpStatus.OK);
    }

    @GetMapping("/supplier/category/toggle")
    public ApiResponse<List<ToggleCodeLabelDto>> getSupplierCategoryToggle() {
        List<ToggleCodeLabelDto> list = List.of(
            new ToggleCodeLabelDto("전체 카테고리", "ALL"),
            new ToggleCodeLabelDto("자재", "MATERIAL"),
            new ToggleCodeLabelDto("품목", "ITEM"),
            new ToggleCodeLabelDto("기타", "ETC")
        );
    return ApiResponse.success(list, "카테고리 목록 조회 성공", org.springframework.http.HttpStatus.OK);
    }

    @GetMapping("/supplier/search-type/toggle")
    public ApiResponse<List<ToggleCodeLabelDto>> getSupplierSearchTypeToggle() {
        List<ToggleCodeLabelDto> list = List.of(
                new ToggleCodeLabelDto("공급 업체명", "SupplierCompanyNumber"),
                new ToggleCodeLabelDto("공급 업체 번호", "SupplierCompanyName")
        );
        return ApiResponse.success(list, "카테고리 목록 조회 성공", org.springframework.http.HttpStatus.OK);
    }

    /**
     * 공급업체별 주문 통계 조회
     */
    @GetMapping("/supplier/orders/statistics")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "공급업체별 주문 통계 조회",
            description = "공급업체 사용자 ID를 기준으로 해당 공급업체의 주문 통계를 조회합니다. 주, 월, 분기, 년 단위로 제공됩니다."
    )
    public ResponseEntity<ApiResponse<SupplierOrderStatisticsResponseDto>> getSupplierOrderStatistics(
            @RequestParam String userId) {
        try {
            SupplierOrderStatisticsResponseDto statistics = mmStatisticsService.getSupplierOrderStatistics(userId);
            return ResponseEntity.ok(ApiResponse.success(statistics, "공급업체 주문 통계 조회 성공", HttpStatus.OK));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("공급업체 주문 통계 조회 실패: " + e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }
}
