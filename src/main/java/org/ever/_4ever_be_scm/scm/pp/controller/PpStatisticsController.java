package org.ever._4ever_be_scm.scm.pp.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.ever._4ever_be_scm.common.response.ApiResponse;
import org.ever._4ever_be_scm.scm.pp.dto.PpStatisticsResponseDto;
import org.ever._4ever_be_scm.scm.pp.service.PpStatisticsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * PP 통계 관리 컨트롤러
 */
@Tag(name = "생산관리", description = "생산 관리 통계 API")
@RestController
@RequestMapping("/scm-pp/pp")
@RequiredArgsConstructor
public class PpStatisticsController {

    private final PpStatisticsService ppStatisticsService;

    /**
     * PP 통계 조회 API
     *
     * @return 생산 관련 통계 정보
     */
    @GetMapping("/statistic")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "PP 통계 조회",
            description = "생산관리의 통계를 반환합니다. 생산중인 품목, 완료된 생산, 완제품 개수 포함"
    )
    public ResponseEntity<ApiResponse<PpStatisticsResponseDto>> getPpStatistic() {
        PpStatisticsResponseDto statistics = ppStatisticsService.getPpStatistics();

        return ResponseEntity.ok(ApiResponse.success(statistics, "생산 통계 정보를 조회했습니다.", HttpStatus.OK));
    }
}
