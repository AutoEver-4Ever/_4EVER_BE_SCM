package org.ever._4ever_be_scm.scm.mm.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.ever._4ever_be_scm.common.response.ApiResponse;
import org.ever._4ever_be_scm.scm.mm.dto.MMStatisticsResponseDto;
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

    @GetMapping("/purchase_requisition/status/toggle")
    public ApiResponse<List<ToggleCodeLabelDto>> getPurchaseRequisitionStatusToggle() {
        List<ToggleCodeLabelDto> list = List.of(
            new ToggleCodeLabelDto("전체", "ALL"),
            new ToggleCodeLabelDto("승인", "APPROVAL"),
            new ToggleCodeLabelDto("대기", "PENDING"),
            new ToggleCodeLabelDto("반려", "REJECTED")
        );
    return ApiResponse.success(list, "상태 목록 조회 성공", org.springframework.http.HttpStatus.OK);
    }

    @GetMapping("/purchase-orders/status/toggle")
    public ApiResponse<List<ToggleCodeLabelDto>> getPurchaseOrderStatusToggle() {
        List<ToggleCodeLabelDto> list = List.of(
            new ToggleCodeLabelDto("전체", "ALL"),
            new ToggleCodeLabelDto("승인", "APPROVAL"),
            new ToggleCodeLabelDto("대기", "PENDING"),
            new ToggleCodeLabelDto("반려", "REJECTED"),
            new ToggleCodeLabelDto("배송중", "DELIVERING"),
            new ToggleCodeLabelDto("배송완료", "DELIVERED")
        );
    return ApiResponse.success(list, "상태 목록 조회 성공", org.springframework.http.HttpStatus.OK);
    }

    @GetMapping("/supplier/status/toggle")
    public ApiResponse<List<ToggleCodeLabelDto>> getSupplierStatusToggle() {
        List<ToggleCodeLabelDto> list = List.of(
            new ToggleCodeLabelDto("전체", "ALL"),
            new ToggleCodeLabelDto("활성", "ACTIVE"),
            new ToggleCodeLabelDto("비활성", "INACTIVE")
        );
    return ApiResponse.success(list, "상태 목록 조회 성공", org.springframework.http.HttpStatus.OK);
    }

    @GetMapping("/supplier/category/toggle")
    public ApiResponse<List<ToggleCodeLabelDto>> getSupplierCategoryToggle() {
        List<ToggleCodeLabelDto> list = List.of(
            new ToggleCodeLabelDto("전체", "ALL"),
            new ToggleCodeLabelDto("자재", "MATERIAL"),
            new ToggleCodeLabelDto("품목", "ITEM"),
            new ToggleCodeLabelDto("기타", "ETC")
        );
    return ApiResponse.success(list, "카테고리 목록 조회 성공", org.springframework.http.HttpStatus.OK);
    }
}
