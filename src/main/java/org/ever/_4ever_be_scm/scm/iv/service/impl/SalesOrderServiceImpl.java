package org.ever._4ever_be_scm.scm.iv.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ever._4ever_be_scm.common.response.ApiResponse;
import org.ever._4ever_be_scm.infrastructure.kafka.config.KafkaTopicConfig;
import org.ever._4ever_be_scm.scm.iv.dto.SalesOrderDetailDto;
import org.ever._4ever_be_scm.scm.iv.dto.SalesOrderDto;
import org.ever._4ever_be_scm.scm.iv.dto.SalesOrderStatusChangeRequestDto;
import org.ever._4ever_be_scm.scm.iv.entity.ProductStock;
import org.ever._4ever_be_scm.scm.iv.integration.dto.SdOrderDetailResponseDto;
import org.ever._4ever_be_scm.scm.iv.integration.dto.SdOrderDto;
import org.ever._4ever_be_scm.scm.iv.integration.dto.SdOrderListResponseDto;
import org.ever._4ever_be_scm.scm.iv.integration.port.SdOrderServicePort;
import org.ever._4ever_be_scm.scm.iv.repository.ProductStockRepository;
import org.ever._4ever_be_scm.scm.iv.service.SalesOrderService;
import org.ever.event.SalesOrderStatusChangeEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.async.DeferredResult;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 판매 주문 관리 서비스 구현
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SalesOrderServiceImpl implements SalesOrderService {

    private final SdOrderServicePort sdOrderServicePort;
    private final org.ever._4ever_be_scm.infrastructure.kafka.producer.KafkaProducerService kafkaProducerService;
    private final org.ever._4ever_be_scm.common.async.GenericAsyncResultManager<Void> asyncResultManager;
    private final ProductStockRepository productStockRepository;
    private final org.ever._4ever_be_scm.scm.iv.service.StockTransferService stockTransferService;

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
                "IN_PRODUCTION"
        );
        
        // DTO 변환
        List<SalesOrderDto> salesOrders = sdResponse.getContent().stream()
                .map(this::convertToInProductionDto)
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
                .map(this::convertToReadyForShipmentDto)
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
    private SalesOrderDto convertToInProductionDto(SdOrderDto sdOrder) {
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
     * SD 서비스의 주문 DTO를 내부 DTO로 변환
     */
    private SalesOrderDto convertToReadyForShipmentDto(SdOrderDto sdOrder) {
        return SalesOrderDto.builder()
                .salesOrderId(sdOrder.getSalesOrderId())
                .salesOrderNumber(sdOrder.getSalesOrderNumber())
                .customerName(sdOrder.getCustomerName())
                .orderDate(sdOrder.getOrderDate())
                .dueDate(sdOrder.getDueDate())
                .totalAmount(sdOrder.getTotalAmount())
                .statusCode("READY_FOR_SHIPMENT")
                .build();
    }
    
    /**
     * SD 서비스의 주문 상세 DTO를 내부 DTO로 변환
     */
    private SalesOrderDetailDto convertToSalesOrderDetailDto(SdOrderDetailResponseDto sdDetail) {
        // 주문 아이템 목록 변환
        List<SalesOrderDetailDto.OrderItemDto> orderItems = sdDetail.getItems().stream()
                .map(item -> SalesOrderDetailDto.OrderItemDto.builder()
                        .itemId(item.getItemId())
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

    @Override
    @Transactional
    public DeferredResult<ResponseEntity<ApiResponse<Void>>> changeSalesOrderStatusAsync(
            String salesOrderId, SalesOrderStatusChangeRequestDto requestDto,String requesterId) {

        // DeferredResult 생성 (타임아웃 30초)
        DeferredResult<ResponseEntity<ApiResponse<Void>>> deferredResult =
                new DeferredResult<>(30000L);

        // 타임아웃 처리
        deferredResult.onTimeout(() -> {
            deferredResult.setResult(ResponseEntity
                    .status(HttpStatus.REQUEST_TIMEOUT)
                    .body(ApiResponse.fail("처리 시간이 초과되었습니다.", HttpStatus.REQUEST_TIMEOUT)));
        });

        try {
            // 1. 주문 상세 정보 조회하여 각 아이템의 수량 확인
            SdOrderDetailResponseDto orderDetail = sdOrderServicePort.getSalesOrderDetail(salesOrderId);
            List<SdOrderDetailResponseDto.SdOrderItemDto> items = orderDetail.getItems();

            // 2. 재고 차감 실행 (reserved stock 먼저, 그 다음 forShipmentCount)
            Map<String, BigDecimal> reducedQuantities = new HashMap<>();

            for (SdOrderDetailResponseDto.SdOrderItemDto item : items) {
                // 아이템 ID가 요청 DTO의 itemIds에 포함된 경우만 처리
                if (requestDto.getItemIds().contains(item.getItemId())) {
                    BigDecimal quantity = BigDecimal.valueOf(item.getQuantity());

                    // ProductStock 조회
                    ProductStock productStock = productStockRepository.findByProductId(item.getItemId())
                            .orElseThrow(() -> new RuntimeException(
                                    "ProductStock을 찾을 수 없습니다: " + item.getItemId()));

                    // 현재 재고 상태
                    BigDecimal currentReserved = productStock.getReservedCount() != null ?
                            productStock.getReservedCount() : BigDecimal.ZERO;
                    BigDecimal currentForShipment = productStock.getForShipmentCount() != null ?
                            productStock.getForShipmentCount() : BigDecimal.ZERO;

                    log.info("재고 차감 시작 - productId={}, quantity={}, reserved={}, forShipment={}",
                            item.getItemId(), quantity, currentReserved, currentForShipment);

                    // 1. 예약 재고 해제 및 forShipmentCount 차감 (출고 처리보다 먼저 실행)
                    BigDecimal remainingQuantity = quantity;
                    // 1-1. 먼저 예약 재고에서 차감
                    if (currentReserved.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal fromReserved = currentReserved.min(remainingQuantity);
                        productStock.releaseReservation(fromReserved);
                        remainingQuantity = remainingQuantity.subtract(fromReserved);

                        log.info("예약 재고 해제 - productId={}, 해제량={}, 남은차감량={}",
                                item.getItemId(), fromReserved, remainingQuantity);
                    }

                    // 1-2. 남은 수량은 forShipmentCount에서 차감
                    if (remainingQuantity.compareTo(BigDecimal.ZERO) > 0) {
                        if (currentForShipment.compareTo(remainingQuantity) < 0) {
                            throw new RuntimeException(
                                    "forShipmentCount가 부족합니다: productId=" + item.getItemId() +
                                            ", 필요=" + remainingQuantity + ", 현재=" + currentForShipment);
                        }

                        BigDecimal newForShipment = currentForShipment.subtract(remainingQuantity);
                        productStock.setForShipmentCount(newForShipment);

                        log.info("forShipmentCount 차감 - productId={}, 차감량={}, 기존={}, 신규={}",
                                item.getItemId(), remainingQuantity, currentForShipment, newForShipment);
                    }

                    // 1-3. DB 저장 (출고 처리 전에 예약 해제를 먼저 저장)
                    productStockRepository.save(productStock);

                    log.info("예약 해제 및 forShipmentCount 차감 완료 - productId={}", item.getItemId());

                    // 2. 출고 처리 (예약 해제 후 실행)
                    stockTransferService.processStockDelivery(
                            item.getItemId(),
                            quantity.negate(), // 음수로 변환 (출고)
                            requesterId, // requesterId
                            salesOrderId, // referenceCode
                            "판매 주문 출하" // reason
                    );

                    reducedQuantities.put(item.getItemId(), quantity);

                    log.info("재고 차감 완료 - productId={}, 총차감량={}", item.getItemId(), quantity);
                }
            }

            // 4. 트랜잭션 ID 생성 및 DeferredResult 등록
            String transactionId = java.util.UUID.randomUUID().toString();
            asyncResultManager.registerResult(transactionId, deferredResult);

            //TODO 알람 필요 - 판매 주문 출하 완료 알림 (담당자, 고객)

            // 5. Business 서버로 상태 변경 이벤트 발행 (itemIds 제거)
            SalesOrderStatusChangeEvent event = SalesOrderStatusChangeEvent.builder()
                    .transactionId(transactionId)
                    .salesOrderId(salesOrderId)
                    .timestamp(System.currentTimeMillis())
                    .build();

            kafkaProducerService.sendToTopic(
                    KafkaTopicConfig.SALES_ORDER_STATUS_CHANGE_TOPIC,
                    salesOrderId, event);

            log.info("판매 주문 상태 변경 이벤트 발행 완료 - transactionId={}, salesOrderId={}",
                    transactionId, salesOrderId);

        } catch (Exception e) {
            log.error("판매 주문 상태 변경 실패 - salesOrderId={}, error={}", salesOrderId, e.getMessage(), e);
            deferredResult.setResult(ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("판매 주문 상태 변경 실패: " + e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR)));
        }

        return deferredResult;
    }
}
