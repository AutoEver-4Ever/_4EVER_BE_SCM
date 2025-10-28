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
     * 생산중 판매 주문 목록 조회 (IN_PRODUCTION 상태)
     * 
     * @param pageable 페이징 정보
     * @return 생산중 판매 주문 목록
     */
    @Override
    public Page<SalesOrderDto> getProductionSalesOrders(Pageable pageable) {
        // 임시 데이터 생성 (IN_PRODUCTION 상태)
        List<SalesOrderDto> salesOrders = Arrays.asList(
            SalesOrderDto.builder()
                .salesOrderId(UUID.randomUUID().toString())
                .salesOrderNumber("SO-2024-001")
                .customerName("대한제철")
                .orderDate("2024-01-10T12:50")
                .dueDate("2024-01-20T17:14")
                .totalAmount(15750000)
                .statusCode("IN_PRODUCTION")
                .build(),
            SalesOrderDto.builder()
                .salesOrderId(UUID.randomUUID().toString())
                .salesOrderNumber("SO-2024-003")
                .customerName("삼성물산")
                .orderDate("2024-01-12T12:30")
                .dueDate("2024-01-25T10:23")
                .totalAmount(8900000)
                .statusCode("IN_PRODUCTION")
                .build(),
            SalesOrderDto.builder()
                .salesOrderId(UUID.randomUUID().toString())
                .salesOrderNumber("SO-2024-005")
                .customerName("현대중공업")
                .orderDate("2024-01-15T16:33")
                .dueDate("2024-01-30T16:44")
                .totalAmount(12300000)
                .statusCode("IN_PRODUCTION")
                .build()
        );
        
        // 페이징 처리
        return new PageImpl<>(salesOrders, pageable, salesOrders.size());
    }
    
    /**
     * 출고 준비완료 판매 주문 목록 조회 (READY_TO_SHIP 상태)
     * 
     * @param pageable 페이징 정보
     * @return 출고 준비완료 판매 주문 목록
     */
    @Override
    public Page<SalesOrderDto> getReadyToShipSalesOrders(Pageable pageable) {
        // 임시 데이터 생성 (READY_TO_SHIP 상태)
        List<SalesOrderDto> salesOrders = Arrays.asList(
            SalesOrderDto.builder()
                .salesOrderId(UUID.randomUUID().toString())
                .salesOrderNumber("SO-2024-002")
                .customerName("현대건설")
                .orderDate("2024-01-11T16:54")
                .dueDate("2024-01-22T08:54")
                .totalAmount(9800000)
                .statusCode("READY_TO_SHIP")
                .build(),
            SalesOrderDto.builder()
                .salesOrderId(UUID.randomUUID().toString())
                .salesOrderNumber("SO-2024-004")
                .customerName("포스코")
                .orderDate("2024-01-13T15:21")
                .dueDate("2024-01-28T09:54")
                .totalAmount(18500000)
                .statusCode("READY_TO_SHIP")
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
            .dueDate("2024-01-22T16:54")
            .statusCode("READY_TO_SHIP")
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
