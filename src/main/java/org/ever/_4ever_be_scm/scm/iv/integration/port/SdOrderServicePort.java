package org.ever._4ever_be_scm.scm.iv.integration.port;

import org.ever._4ever_be_scm.scm.iv.integration.dto.SdOrderDetailResponseDto;
import org.ever._4ever_be_scm.scm.iv.integration.dto.SdOrderListResponseDto;

public interface SdOrderServicePort {
    
    /**
     * SD 서비스에서 판매 주문 목록을 조회합니다.
     * 
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param status 주문 상태 (IN_PRODUCTION, READY_FOR_SHIPMENT 등)
     * @return 판매 주문 목록
     */
    SdOrderListResponseDto getSalesOrderList(int page, int size, String status);
    
    /**
     * SD 서비스에서 판매 주문 상세 정보를 조회합니다.
     * 
     * @param salesOrderId 판매 주문 ID
     * @return 판매 주문 상세 정보
     */
    SdOrderDetailResponseDto getSalesOrderDetail(String salesOrderId);
}
