package org.ever._4ever_be_scm.scm.service;

import org.ever._4ever_be_scm.scm.dto.StockTransferDto;
import org.ever._4ever_be_scm.scm.dto.StockTransferDetailDto;
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
     * 재고 이동 상세 목록 조회
     * 
     * @param pageable 페이징 정보
     * @return 재고 이동 상세 목록
     */
    Page<StockTransferDetailDto> getStockTransfersDetail(Pageable pageable);
}
