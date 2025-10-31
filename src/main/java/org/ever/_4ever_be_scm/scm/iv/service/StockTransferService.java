package org.ever._4ever_be_scm.scm.iv.service;

import org.ever._4ever_be_scm.scm.iv.dto.StockDeliveryRequestDto;
import org.ever._4ever_be_scm.scm.iv.dto.StockTransferDto;
import org.ever._4ever_be_scm.scm.iv.dto.StockTransferRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 재고 이동 관리 서비스 인터페이스
 */
public interface StockTransferService {
    /**
     * 재고 이동 목록 조회
     * 
     * @param pageable 페이징 정보
     * @return 재고 이동 목록
     */
    Page<StockTransferDto> getStockTransfers(Pageable pageable);
    
    /**
     * 창고간 재고 이동 생성
     * 
     * @param request 재고 이동 요청 정보
     */
    void createStockTransfer(StockTransferRequestDto request);
    
    /**
     * 재고 입출고 처리
     * 
     * @param request 재고 입출고 요청 정보
     */
    void processStockDelivery(StockDeliveryRequestDto request);

}
