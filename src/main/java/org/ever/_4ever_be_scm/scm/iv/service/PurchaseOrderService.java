package org.ever._4ever_be_scm.scm.iv.service;

import org.ever._4ever_be_scm.scm.iv.dto.PurchaseOrderDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 구매 발주 관리 서비스 인터페이스
 */
public interface PurchaseOrderService {
    
    /**
     * 입고 상태별 발주 목록 조회
     * 
     * @param status 상태 (입고 대기, 입고 완료)
     * @param pageable 페이징 정보
     * @return 입고 상태별 발주 목록
     */
    Page<PurchaseOrderDto> getPurchaseOrdersByStatus(String status, Pageable pageable);
}
