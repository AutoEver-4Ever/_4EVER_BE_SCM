package org.ever._4ever_be_scm.scm.iv.service;

import org.ever._4ever_be_scm.common.response.ApiResponse;
import org.ever._4ever_be_scm.scm.iv.dto.SalesOrderDetailDto;
import org.ever._4ever_be_scm.scm.iv.dto.SalesOrderDto;
import org.ever._4ever_be_scm.scm.iv.dto.SalesOrderStatusChangeRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * 판매 주문 관리 서비스 인터페이스
 */
public interface SalesOrderService {
    /**
     * 생산중 판매 주문 목록 조회 (IN_PRODUCTION 상태)
     *
     * @param pageable 페이징 정보
     * @return 생산중 판매 주문 목록
     */
    Page<SalesOrderDto> getProductionSalesOrders(Pageable pageable);

    /**
     * 출고 준비완료 판매 주문 목록 조회 (READY_TO_SHIP 상태)
     *
     * @param pageable 페이징 정보
     * @return 출고 준비완료 판매 주문 목록
     */
    Page<SalesOrderDto> getReadyToShipSalesOrders(Pageable pageable);

    /**
     * 출고 준비 완료 판매 주문 상세 조회
     *
     * @param salesOrderId 판매 주문 ID
     * @return 출고 준비 완료 판매 주문 상세 정보
     */
    SalesOrderDetailDto getReadyToShipOrderDetail(String salesOrderId);

    /**
     * 생산중 판매 주문 상세 조회
     *
     * @param salesOrderId 판매 주문 ID
     * @return 생산중 판매 주문 상세 정보
     */
    SalesOrderDetailDto getProductionDetail(String salesOrderId);

    /**
     * 판매 주문 상태 변경 (비동기 - 분산 트랜잭션)
     *
     * @param salesOrderId 판매 주문 ID
     * @param requestDto 요청 DTO (itemIds)
     * @return DeferredResult
     */
    DeferredResult<ResponseEntity<ApiResponse<Void>>> changeSalesOrderStatusAsync(String salesOrderId, SalesOrderStatusChangeRequestDto requestDto);
}
