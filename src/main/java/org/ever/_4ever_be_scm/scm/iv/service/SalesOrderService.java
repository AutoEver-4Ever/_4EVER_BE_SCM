package org.ever._4ever_be_scm.scm.iv.service;

import org.ever._4ever_be_scm.scm.iv.dto.SalesOrderDetailDto;
import org.ever._4ever_be_scm.scm.iv.dto.SalesOrderDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 판매 주문 관리 서비스 인터페이스
 */
public interface SalesOrderService {
    
    /**
     * 상태별 판매 주문 목록 조회
     * 
     * @param status 상태 (생산중, 출고준비완료, 배송중)
     * @param pageable 페이징 정보
     * @return 상태별 판매 주문 목록
     */
    Page<SalesOrderDto> getSalesOrdersByStatus(String status, Pageable pageable);
    
    /**
     * 출고 준비 완료 판매 주문 상세 조회
     * 
     * @param salesOrderId 판매 주문 ID
     * @return 출고 준비 완료 판매 주문 상세 정보
     */
    SalesOrderDetailDto getReadyToShipOrderDetail(String salesOrderId);
}
