package org.ever._4ever_be_scm.scm.pp.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ever._4ever_be_scm.common.response.ApiResponse;
import org.ever._4ever_be_scm.scm.pp.service.DashboardService;
import org.ever._4ever_be_scm.scm.pp.service.dto.DashboardWorkflowItemDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/scm-pp/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/purchase-orders/supplier")
    public ApiResponse<List<DashboardWorkflowItemDto>> getSupplierPurchaseOrders(
            @RequestParam("userId") String userId,
            @RequestParam(value = "size", defaultValue = "5") int size
    ) {
        return ApiResponse.success(
                dashboardService.getSupplierPurchaseOrders(userId, size),
                "공급사 발주서 목록 조회 성공",
                HttpStatus.OK
        );
    }

    @GetMapping("/purchase-requests")
    public ApiResponse<List<DashboardWorkflowItemDto>> getPurchaseRequests(
            @RequestParam("userId") String userId,
            @RequestParam(value = "size", defaultValue = "5") int size
    ) {
        return ApiResponse.success(
                dashboardService.getPurchaseRequests(userId, size),
                "구매 요청 목록 조회 성공",
                HttpStatus.OK
        );
    }
}
