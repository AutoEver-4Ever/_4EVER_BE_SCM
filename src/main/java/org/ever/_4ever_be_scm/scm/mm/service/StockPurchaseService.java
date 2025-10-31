package org.ever._4ever_be_scm.scm.mm.service;

import org.ever._4ever_be_scm.scm.mm.dto.StockPurchaseRequestDto;

public interface StockPurchaseService {
    
    /**
     * 재고성 구매요청 생성
     */
    String createStockPurchaseRequest(StockPurchaseRequestDto requestDto, String requesterId);
}
