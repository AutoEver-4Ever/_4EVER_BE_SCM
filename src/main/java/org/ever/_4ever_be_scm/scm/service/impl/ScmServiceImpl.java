package org.ever._4ever_be_scm.scm.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ever._4ever_be_scm.common.exception.ErrorCode;
import org.ever._4ever_be_scm.common.exception.ScmException;
import org.ever._4ever_be_scm.scm.dto.response.ScmResponseDto;
import org.ever._4ever_be_scm.scm.service.ScmService;
import org.ever._4ever_be_scm.scm.vo.ScmRequestVo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScmServiceImpl implements ScmService {

    // 임시 저장소 (실제로는 DB 사용)
    private final List<ScmResponseDto> scmTransactions = new ArrayList<>();

    @Override
    public ScmResponseDto reserveStock(ScmRequestVo request) {
        log.info("재고 예약 시작 - orderId: {}, productId: {}, quantity: {}",
            request.getOrderId(), request.getProductId(), request.getQuantity());

        // 수량 검증
        if (request.getQuantity() <= 0) {
            throw new ScmException(ErrorCode.INVALID_QUANTITY,
                "유효하지 않은 수량입니다: " + request.getQuantity());
        }

        // 창고 검증
        if (!isValidWarehouse(request.getWarehouseId())) {
            throw new ScmException(ErrorCode.INVALID_WAREHOUSE,
                "유효하지 않은 창고입니다: " + request.getWarehouseId());
        }

        // 재고 예약 처리 (실제로는 DB 트랜잭션)
        String scmId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        String status = "RESERVED";

        // 비즈니스 로직 결과로 DTO 생성
        ScmResponseDto scmTransaction = ScmResponseDto.builder()
            .scmId(scmId)
            .orderId(request.getOrderId())
            .productId(request.getProductId())
            .quantity(request.getQuantity())
            .warehouseId(request.getWarehouseId())
            .status(status)
            .description(request.getDescription())
            .createdAt(now)
            .updatedAt(now)
            .build();

        scmTransactions.add(scmTransaction);
        log.info("재고 예약 완료 - scmId: {}", scmTransaction.getScmId());

        return scmTransaction;
    }

    @Override
    public ScmResponseDto getScmTransaction(String scmId) {
        log.info("SCM 트랜잭션 조회 - scmId: {}", scmId);

        return scmTransactions.stream()
            .filter(t -> t.getScmId().equals(scmId))
            .findFirst()
            .orElseThrow(() -> new ScmException(ErrorCode.SCM_NOT_FOUND,
                "SCM 트랜잭션을 찾을 수 없습니다: " + scmId));
    }

    @Override
    public List<ScmResponseDto> getAllScmTransactions() {
        log.info("전체 SCM 트랜잭션 조회 - 총 개수: {}", scmTransactions.size());
        return new ArrayList<>(scmTransactions);
    }

    @Override
    public ScmResponseDto releaseStock(String scmId) {
        log.info("재고 해제 시작 - scmId: {}", scmId);

        ScmResponseDto transaction = scmTransactions.stream()
            .filter(t -> t.getScmId().equals(scmId))
            .findFirst()
            .orElseThrow(() -> new ScmException(ErrorCode.SCM_NOT_FOUND,
                "SCM 트랜잭션을 찾을 수 없습니다: " + scmId));

        if ("RELEASED".equals(transaction.getStatus())) {
            throw new ScmException(ErrorCode.STOCK_ALREADY_RELEASED,
                "이미 해제된 재고입니다: " + scmId);
        }

        // 재고 해제 비즈니스 로직 처리
        scmTransactions.removeIf(t -> t.getScmId().equals(scmId));

        // 해제된 재고 정보로 DTO 생성
        ScmResponseDto releasedTransaction = ScmResponseDto.builder()
            .scmId(transaction.getScmId())
            .orderId(transaction.getOrderId())
            .productId(transaction.getProductId())
            .quantity(transaction.getQuantity())
            .warehouseId(transaction.getWarehouseId())
            .status("RELEASED")
            .description(transaction.getDescription())
            .createdAt(transaction.getCreatedAt())
            .updatedAt(LocalDateTime.now())
            .build();

        scmTransactions.add(releasedTransaction);

        log.info("재고 해제 완료 - scmId: {}", scmId);
        return releasedTransaction;
    }

    private boolean isValidWarehouse(String warehouseId) {
        // 실제로는 DB 조회
        return List.of("warehouse-001", "warehouse-002", "warehouse-003").contains(warehouseId);
    }
}
