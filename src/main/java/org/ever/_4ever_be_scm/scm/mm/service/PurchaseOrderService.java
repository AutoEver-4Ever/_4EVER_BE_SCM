package org.ever._4ever_be_scm.scm.mm.service;

import org.ever._4ever_be_scm.scm.mm.dto.PurchaseOrderDetailResponseDto;
import org.ever._4ever_be_scm.scm.mm.dto.PurchaseOrderListResponseDto;
import org.ever._4ever_be_scm.scm.mm.vo.PurchaseOrderSearchVo;
import org.springframework.data.domain.Page;

public interface PurchaseOrderService {
    Page<PurchaseOrderListResponseDto> getPurchaseOrderList(PurchaseOrderSearchVo searchVo);
    
    PurchaseOrderDetailResponseDto getPurchaseOrderDetail(String purchaseOrderId);
    
    /**
     * 발주서 승인
     */
    void approvePurchaseOrder(String purchaseOrderId, String requesterId);
    
    /**
     * 발주서 반려
     */
    void rejectPurchaseOrder(String purchaseOrderId,String requesterId, String reason);
}
