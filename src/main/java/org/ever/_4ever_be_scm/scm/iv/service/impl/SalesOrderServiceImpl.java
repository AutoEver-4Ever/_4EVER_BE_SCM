package org.ever._4ever_be_scm.scm.iv.service.impl;

import lombok.RequiredArgsConstructor;
import org.ever._4ever_be_scm.scm.iv.dto.SalesOrderDetailDto;
import org.ever._4ever_be_scm.scm.iv.dto.SalesOrderDto;
import org.ever._4ever_be_scm.scm.iv.integration.dto.SdOrderDetailResponseDto;
import org.ever._4ever_be_scm.scm.iv.integration.dto.SdOrderDto;
import org.ever._4ever_be_scm.scm.iv.integration.dto.SdOrderListResponseDto;
import org.ever._4ever_be_scm.scm.iv.integration.port.SdOrderServicePort;
import org.ever._4ever_be_scm.scm.iv.service.SalesOrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 판매 주문 관리 서비스 구현
 */
@Service
@RequiredArgsConstructor
public class SalesOrderServiceImpl implements SalesOrderService {
    
    private final SdOrderServicePort sdOrderServicePort;

    /**
     * 생산중 판매 주문 목록 조회 (IN_PRODUCTION 상태)
     * 
     * @param pageable 페이징 정보
     * @return 생산중 판매 주문 목록
     */
    @Override
    public Page<SalesOrderDto> getProductionSalesOrders(Pageable pageable) {
        // SD 서비스에서 IN_PRODUCTION 상태의 주문 목록 조회
        SdOrderListResponseDto sdResponse = sdOrderServicePort.getSalesOrderList(
                pageable.getPageNumber(), 
                pageable.getPageSize(), 
                "IN_PROGRESS"
        );
        
        // DTO 변환
        List<SalesOrderDto> salesOrders = sdResponse.getContent().stream()
                .map(this::convertToSalesOrderDto)
                .collect(Collectors.toList());
        
        // 페이지 정보 매핑
        long totalElements = sdResponse.getPage().getTotalElements();
        
        return new PageImpl<>(salesOrders, pageable, totalElements);
    }

    /**
     * 출고 준비완료 판매 주문 목록 조회 (READY_FOR_SHIPMENT 상태)
     * 
     * @param pageable 페이징 정보
     * @return 출고 준비완료 판매 주문 목록
     */
    @Override
    public Page<SalesOrderDto> getReadyToShipSalesOrders(Pageable pageable) {
        // SD 서비스에서 READY_FOR_SHIPMENT 상태의 주문 목록 조회
        SdOrderListResponseDto sdResponse = sdOrderServicePort.getSalesOrderList(
                pageable.getPageNumber(), 
                pageable.getPageSize(), 
                "READY_FOR_SHIPMENT"
        );
        
        // DTO 변환
        List<SalesOrderDto> salesOrders = sdResponse.getContent().stream()
                .map(this::convertToSalesOrderDto)
                .collect(Collectors.toList());
        
        // 페이지 정보 매핑
        long totalElements = sdResponse.getPage().getTotalElements();
        
        return new PageImpl<>(salesOrders, pageable, totalElements);
    }

    /**
     * 출고 준비완료 주문 상세 조회
     * 
     * @param salesOrderId 판매 주문 ID
     * @return 출고 준비완료 주문 상세 정보
     */
    @Override
    public SalesOrderDetailDto getReadyToShipOrderDetail(String salesOrderId) {
        // SD 서비스에서 주문 상세 정보 조회
        SdOrderDetailResponseDto sdDetail = sdOrderServicePort.getSalesOrderDetail(salesOrderId);
        
        return convertToSalesOrderDetailDto(sdDetail);
    }

    /**
     * 생산중 주문 상세 조회
     * 
     * @param salesOrderId 판매 주문 ID
     * @return 생산중 주문 상세 정보
     */
    @Override
    public SalesOrderDetailDto getProductionDetail(String salesOrderId) {
        // SD 서비스에서 주문 상세 정보 조회
        SdOrderDetailResponseDto sdDetail = sdOrderServicePort.getSalesOrderDetail(salesOrderId);
        
        return convertToSalesOrderDetailDto(sdDetail);
    }
    
    /**
     * SD 서비스의 주문 DTO를 내부 DTO로 변환
     */
    private SalesOrderDto convertToSalesOrderDto(SdOrderDto sdOrder) {
        return SalesOrderDto.builder()
                .salesOrderId(sdOrder.getSalesOrderId())
                .salesOrderNumber(sdOrder.getSalesOrderNumber())
                .customerName(sdOrder.getCustomerName())
                .orderDate(sdOrder.getOrderDate())
                .dueDate(sdOrder.getDueDate())
                .totalAmount(sdOrder.getTotalAmount())
                .statusCode("IN_PRODUCTION")
                .build();
    }
    
    /**
     * SD 서비스의 주문 상세 DTO를 내부 DTO로 변환
     */
    private SalesOrderDetailDto convertToSalesOrderDetailDto(SdOrderDetailResponseDto sdDetail) {
        // 주문 아이템 목록 변환
        List<SalesOrderDetailDto.OrderItemDto> orderItems = sdDetail.getItems().stream()
                .map(item -> SalesOrderDetailDto.OrderItemDto.builder()
                        .itemName(item.getItemName())
                        .quantity(item.getQuantity())
                        .uomName(item.getUonName())
                        .build())
                .collect(Collectors.toList());
        
        return SalesOrderDetailDto.builder()
                .salesOrderId(sdDetail.getOrder().getSalesOrderId())
                .salesOrderNumber(sdDetail.getOrder().getSalesOrderNumber())
                .customerCompanyName(sdDetail.getCustomer().getCustomerName())
                .dueDate(sdDetail.getOrder().getDueDate())
                .statusCode(sdDetail.getOrder().getStatusCode())
                .orderItems(orderItems)
                .build();
    }
}
