package org.ever._4ever_be_scm.scm.mm.service;

import org.ever._4ever_be_scm.scm.mm.dto.*;
import org.ever._4ever_be_scm.scm.mm.vo.PurchaseRequisitionCreateVo;
import org.ever._4ever_be_scm.scm.mm.vo.PurchaseRequisitionSearchVo;
import org.springframework.data.domain.Page;

public interface PurchaseRequisitionService {
    Page<PurchaseRequisitionListResponseDto> getPurchaseRequisitionList(PurchaseRequisitionSearchVo searchVo);
    
    PurchaseRequisitionDetailResponseDto getPurchaseRequisitionDetail(String purchaseRequisitionId);
    
    void createPurchaseRequisition(PurchaseRequisitionCreateVo createVo);
    
    void approvePurchaseRequisition(String purchaseRequisitionId);
    
    void rejectPurchaseRequisition(String purchaseRequisitionId, PurchaseRequisitionRejectRequestDto requestDto);
}
