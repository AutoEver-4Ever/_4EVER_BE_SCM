package org.ever._4ever_be_scm.scm.pp.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ever._4ever_be_scm.common.response.ApiResponse;
import org.ever._4ever_be_scm.scm.pp.service.DashboardService;
import org.ever._4ever_be_scm.scm.pp.service.dto.DashboardWorkflowItemDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/scm-pp/dashboard/quotations")
@RequiredArgsConstructor
public class ProductionDashboardController {

    private final DashboardService dashboardService;

    /**
     * 생산관리로 전환된 견적서 목록 조회
     */
    @GetMapping("/production")
    public ApiResponse<List<DashboardWorkflowItemDto>> getQuotationsToProduction(
            @RequestParam("userId") String userId,
            @RequestParam(value = "size", defaultValue = "5") int size
    ) {
        log.info("[SCM-PP][Dashboard] 생산 전환 견적 목록 조회 - userId: {}, size: {}", userId, size);
        List<DashboardWorkflowItemDto> items = dashboardService.getQuotationsToProduction(userId, size);
        return ApiResponse.success(items, "생산 전환 견적 목록을 조회했습니다.", HttpStatus.OK);
    }

    /**
     * 생산 진행(MES) 목록 조회
     */
    @GetMapping("/production/in-progress")
    public ApiResponse<List<DashboardWorkflowItemDto>> getProductionInProgress(
            @RequestParam("userId") String userId,
            @RequestParam(value = "size", defaultValue = "5") int size
    ) {
        log.info("[SCM-PP][Dashboard] 생산 진행 목록 조회 - userId: {}, size: {}", userId, size);
        List<DashboardWorkflowItemDto> items = dashboardService.getProductionInProgress(userId, size);
        return ApiResponse.success(items, "생산 진행 목록을 조회했습니다.", HttpStatus.OK);
    }
}
