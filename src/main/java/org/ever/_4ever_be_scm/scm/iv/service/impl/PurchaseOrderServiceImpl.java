package org.ever._4ever_be_scm.scm.iv.service.impl;

import org.ever._4ever_be_scm.scm.iv.dto.PurchaseOrderDto;
import org.ever._4ever_be_scm.scm.iv.service.PurchaseOrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 구매 발주 관리 서비스 구현
 */
@Service
public class PurchaseOrderServiceImpl implements PurchaseOrderService {
    
    /**
     * 입고 상태별 발주 목록 조회
     * 
     * @param status 상태 (입고 대기, 입고 완료)
     * @param pageable 페이징 정보
     * @return 입고 상태별 발주 목록
     */
    @Override
    public Page<PurchaseOrderDto> getPurchaseOrdersByStatus(String status, Pageable pageable) {
        // 임시 데이터 생성 (추후 레포지토리 연결 예정)
        List<PurchaseOrderDto> purchaseOrders = Arrays.asList(
            PurchaseOrderDto.builder()
                .purchaseOrderId(UUID.randomUUID().toString())
                .purchaseOrderCode("PO-2024-001")
                .supplier("스테인리스코리아")
                .orderDate("2024-01-10")
                .dueDate("2024-01-16")
                .totalAmount(4250000)
                .status(status)
                .build(),
            PurchaseOrderDto.builder()
                .purchaseOrderId(UUID.randomUUID().toString())
                .purchaseOrderCode("PO-2024-002")
                .supplier("금속유통")
                .orderDate("2024-01-11")
                .dueDate("2024-01-17")
                .totalAmount(1860000)
                .status(status)
                .build()
        );
        
        // 페이징 처리
        return new PageImpl<>(purchaseOrders, pageable, purchaseOrders.size());
    }
}
