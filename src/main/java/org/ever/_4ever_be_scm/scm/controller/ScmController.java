package org.ever._4ever_be_scm.scm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ever._4ever_be_scm.common.response.ApiResponse;
import org.ever._4ever_be_scm.scm.dto.request.ScmRequestDto;
import org.ever._4ever_be_scm.scm.dto.response.ScmResponseDto;
import org.ever._4ever_be_scm.scm.service.ScmService;
import org.ever._4ever_be_scm.scm.vo.ScmRequestVo;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/scm")
@RequiredArgsConstructor
public class ScmController {

    private final ScmService scmService;

    @PostMapping("/reserve")
    public ApiResponse<ScmResponseDto> reserveStock(@Valid @RequestBody ScmRequestDto requestDto) {
        log.info("재고 예약 요청 수신 - orderId: {}, productId: {}, quantity: {}",
            requestDto.getOrderId(), requestDto.getProductId(), requestDto.getQuantity());

        ScmRequestVo requestVo = new ScmRequestVo(
            requestDto.getOrderId(),
            requestDto.getProductId(),
            requestDto.getQuantity(),
            requestDto.getWarehouseId(),
            requestDto.getDescription()
        );

        ScmResponseDto responseDto = scmService.reserveStock(requestVo);
        return ApiResponse.success(responseDto, "재고가 성공적으로 예약되었습니다.", HttpStatus.CREATED);
    }

    @GetMapping("/{scmId}")
    public ApiResponse<ScmResponseDto> getScmTransaction(@PathVariable String scmId) {
        log.info("SCM 트랜잭션 조회 요청 - scmId: {}", scmId);
        ScmResponseDto responseDto = scmService.getScmTransaction(scmId);
        return ApiResponse.success(responseDto, "SCM 트랜잭션 조회 성공", HttpStatus.OK);
    }

    @GetMapping
    public ApiResponse<List<ScmResponseDto>> getAllScmTransactions() {
        log.info("전체 SCM 트랜잭션 조회 요청");
        List<ScmResponseDto> responseDtoList = scmService.getAllScmTransactions();
        return ApiResponse.success(responseDtoList, "전체 SCM 트랜잭션 조회 성공", HttpStatus.OK);
    }

    @DeleteMapping("/{scmId}")
    public ApiResponse<ScmResponseDto> releaseStock(@PathVariable String scmId) {
        log.info("재고 해제 요청 - scmId: {}", scmId);
        ScmResponseDto responseDto = scmService.releaseStock(scmId);
        return ApiResponse.success(responseDto, "재고가 성공적으로 해제되었습니다.", HttpStatus.OK);
    }

    @GetMapping("/health")
    public ApiResponse<String> healthCheck() {
        return ApiResponse.success("SCM Service is running", "헬스 체크 성공", HttpStatus.OK);
    }
}
