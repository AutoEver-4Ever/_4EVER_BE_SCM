package org.ever._4ever_be_scm.scm.mm.service;

import org.ever._4ever_be_scm.common.response.ApiResponse;
import org.ever._4ever_be_scm.scm.mm.dto.PurchaseOrderDetailResponseDto;
import org.ever._4ever_be_scm.scm.mm.dto.PurchaseOrderListResponseDto;
import org.ever._4ever_be_scm.scm.mm.vo.PurchaseOrderSearchVo;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;

public interface PurchaseOrderService {
    Page<PurchaseOrderListResponseDto> getPurchaseOrderList(PurchaseOrderSearchVo searchVo);

    Page<PurchaseOrderListResponseDto> getPurchaseOrderListBySupplier(String userId, PurchaseOrderSearchVo searchVo);

    PurchaseOrderDetailResponseDto getPurchaseOrderDetail(String purchaseOrderId);

    /**
     * 발주서 승인 (비동기 - 분산 트랜잭션)
     */
    DeferredResult<ResponseEntity<ApiResponse<Void>>> approvePurchaseOrderAsync(String purchaseOrderId, String requesterId);
    
    /**
     * 발주서 반려
     */
    void rejectPurchaseOrder(String purchaseOrderId,String requesterId, String reason);

    /**
     * 배송 시작
     */
    void startDelivery(String purchaseOrderId);

    /**
     * 입고 완료
     */
    void completeDelivery(String purchaseOrderId);
}
