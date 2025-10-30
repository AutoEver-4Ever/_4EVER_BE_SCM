package org.ever._4ever_be_scm.scm.iv.service;

import org.ever._4ever_be_scm.scm.iv.dto.PurchaseOrderDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

/**
 * 구매 발주 관리 서비스 인터페이스
 */
public interface PurchaseOrdersService {
    
    /**
     * 입고 준비 목록 조회 (RECEIVING 상태)
     * 
     * @param pageable 페이징 정보
     * @return 입고 준비 발주 목록
     */
    Page<PurchaseOrderDto> getReceivingPurchaseOrders(Pageable pageable);
    
    /**
     * 입고 완료 목록 조회 (RECEIVED 상태) - 날짜 필터링 포함
     * 
     * @param pageable 페이징 정보
     * @param startDate 시작일 (선택사항)
     * @param endDate 종료일 (선택사항)
     * @return 입고 완료 발주 목록
     */
    Page<PurchaseOrderDto> getReceivedPurchaseOrders(Pageable pageable, LocalDate startDate, LocalDate endDate);
}
