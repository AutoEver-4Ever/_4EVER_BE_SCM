package org.ever._4ever_be_scm.scm.iv.service.impl;

import org.ever._4ever_be_scm.scm.iv.dto.SalesOrderDetailDto;
import org.ever._4ever_be_scm.scm.iv.dto.SalesOrderDto;
import org.ever._4ever_be_scm.scm.iv.service.SalesOrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 판매 주문 관리 서비스 구현
 */
@Service
public class SalesOrderServiceImpl implements SalesOrderService {
    
    /**
     * 상태별 판매 주문 목록 조회
     * 
     * @param status 상태 (생산중, 출고준비완료, 배송중)
     * @param pageable 페이징 정보
     * @return 상태별 판매 주문 목록
     */
    @Override
    public Page<SalesOrderDto> getSalesOrdersByStatus(String status, Pageable pageable) {
        // 임시 데이터 생성 (추후 레포지토리 연결 예정)
        List<SalesOrderDto> salesOrders = Arrays.asList(
            SalesOrderDto.builder()
                .salesOrderId(UUID.randomUUID().toString())
                .salesOrderNumber("SO-2024-001")
                .customerName("대한제철")
                .orderDate("2024-01-10")
                .dueDate("2024-01-20")
                .totalAmount(15750000)
                .statusCode(status)
                .build(),
            SalesOrderDto.builder()
                .salesOrderId(UUID.randomUUID().toString())
                .salesOrderNumber("SO-2024-003")
                .customerName("삼성물산")
                .orderDate("2024-01-12")
                .dueDate("2024-01-25")
                .totalAmount(8900000)
                .statusCode(status)
                .build()
        );
        
        // 페이징 처리
        return new PageImpl<>(salesOrders, pageable, salesOrders.size());
    }
    
    /**
     * 출고 준비 완료 판매 주문 상세 조회
     * 
     * @param salesOrderId 판매 주문 ID
     * @return 출고 준비 완료 판매 주문 상세 정보
     */
    @Override
    public SalesOrderDetailDto getReadyToShipOrderDetail(String salesOrderId) {
        // 임시 데이터 생성 (추후 레포지토리 연결 예정)
        return SalesOrderDetailDto.builder()
            .salesOrderId(salesOrderId)
            .salesOrderNumber("SO-2024-002")
            .customerCompanyName("현대건설")
            .dueDate("2024-01-22")
            .statusCode("출고 준비완료")
            .orderItems(Arrays.asList(
                SalesOrderDetailDto.OrderItemDto.builder()
                    .itemName("볼트 M8x20")
                    .quantity(500)
                    .uomName("EA")
                    .build(),
                SalesOrderDetailDto.OrderItemDto.builder()
                    .itemName("베어링 6205")
                    .quantity(20)
                    .uomName("EA")
                    .build()
            ))
            .build();
    }
}
