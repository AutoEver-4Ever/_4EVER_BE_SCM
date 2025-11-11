package org.ever._4ever_be_scm.scm.mm.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ever._4ever_be_scm.common.response.ApiResponse;
import org.ever._4ever_be_scm.infrastructure.kafka.config.KafkaTopicConfig;
import org.ever._4ever_be_scm.scm.iv.entity.Product;
import org.ever._4ever_be_scm.scm.iv.entity.ProductStock;
import org.ever._4ever_be_scm.scm.iv.entity.SupplierCompany;
import org.ever._4ever_be_scm.scm.iv.entity.SupplierUser;
import org.ever._4ever_be_scm.scm.iv.repository.ProductRepository;
import org.ever._4ever_be_scm.scm.mm.dto.PurchaseOrderDetailResponseDto;
import org.ever._4ever_be_scm.scm.mm.dto.PurchaseOrderListResponseDto;
import org.ever._4ever_be_scm.scm.mm.entity.ProductOrder;
import org.ever._4ever_be_scm.scm.mm.entity.ProductOrderApproval;
import org.ever._4ever_be_scm.scm.mm.entity.ProductOrderItem;
import org.ever._4ever_be_scm.scm.mm.repository.ProductOrderApprovalRepository;
import org.ever._4ever_be_scm.scm.mm.repository.ProductOrderItemRepository;
import org.ever._4ever_be_scm.scm.mm.repository.ProductOrderRepository;
import org.ever._4ever_be_scm.scm.mm.service.PurchaseOrderService;
import org.ever._4ever_be_scm.scm.mm.vo.PurchaseOrderSearchVo;
import org.ever.event.PurchaseOrderApprovalEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.async.DeferredResult;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final ProductOrderRepository productOrderRepository;
    private final ProductOrderItemRepository productOrderItemRepository;
    private final ProductRepository productRepository;
    private final ProductOrderApprovalRepository productOrderApprovalRepository;
    private final org.ever._4ever_be_scm.scm.pp.repository.MrpRunRepository mrpRunRepository;
    private final org.ever._4ever_be_scm.scm.pp.repository.MrpRepository mrpRepository;
    private final org.ever._4ever_be_scm.scm.iv.repository.ProductStockRepository productStockRepository;
    private final org.ever._4ever_be_scm.scm.mm.repository.ProductOrderShipmentRepository productOrderShipmentRepository;
    private final org.ever._4ever_be_scm.infrastructure.kafka.producer.KafkaProducerService kafkaProducerService;
    private final org.ever._4ever_be_scm.common.async.GenericAsyncResultManager<Void> asyncResultManager;
    private final org.ever._4ever_be_scm.scm.iv.service.StockTransferService stockTransferService;
    private final org.ever._4ever_be_scm.scm.iv.repository.SupplierCompanyRepository supplierCompanyRepository;
    private final org.ever._4ever_be_scm.infrastructure.redis.service.DeliverySchedulerService deliverySchedulerService;

    @Override
    @Transactional(readOnly = true)  
    public Page<PurchaseOrderListResponseDto> getPurchaseOrderList(PurchaseOrderSearchVo searchVo) {
        PageRequest pageRequest = PageRequest.of(searchVo.getPage(), searchVo.getSize());
        
        // 날짜 범위 설정
        final LocalDateTime startDateTime = searchVo.getStartDate() != null 
                ? searchVo.getStartDate().atStartOfDay() 
                : null;
        final LocalDateTime endDateTime = searchVo.getEndDate() != null 
                ? searchVo.getEndDate().atTime(LocalTime.MAX) 
                : null;
        
        // 모든 발주서 조회 후 필터링
        List<ProductOrder> allOrders = productOrderRepository.findAll();
        List<ProductOrder> filteredOrders = allOrders.stream()
                .filter(order -> {
                    // statusCode 필터링
                    String statusCode = searchVo.getStatusCode();
                    if (statusCode != null && !"ALL".equalsIgnoreCase(statusCode)) {
                        String orderStatusCode = order.getApprovalId() != null 
                            ? order.getApprovalId().getApprovalStatus() 
                            : "PENDING";
                        if (orderStatusCode == null) {
                            orderStatusCode = "PENDING";
                        }
                        if (!statusCode.equalsIgnoreCase(orderStatusCode)) {
                            return false;
                        }
                    }
                    
                    // type/keyword 필터링
                    String type = searchVo.getType();
                    String keyword = searchVo.getKeyword();
                    if (keyword != null && !keyword.isEmpty()) {
                        if ("SupplierCompanyName".equalsIgnoreCase(type)) {
                            // 첫 번째 아이템의 제품에서 supplierCompany를 찾아서 이름으로 매칭
                            List<ProductOrderItem> items = productOrderItemRepository.findByProductOrderId(order.getId());
                            if (items.isEmpty()) return false;
                            ProductOrderItem first = items.get(0);
                            Product product = productRepository.findById(first.getProductId()).orElse(null);
                            String supplierName = null;
                            if (product != null && product.getSupplierCompany() != null) {
                                supplierName = product.getSupplierCompany().getCompanyName();
                            }
                            if (supplierName == null || !supplierName.toLowerCase().contains(keyword.toLowerCase())) return false;
                        } else if ("PurchaseOrderNumber".equalsIgnoreCase(type)) {
                            if (order.getProductOrderCode() == null || !order.getProductOrderCode().toLowerCase().contains(keyword.toLowerCase())) return false;
                        }
                    }

                    // 날짜 범위 필터링
                    if (startDateTime != null && order.getCreatedAt().isBefore(startDateTime)) {
                        return false;
                    }
                    if (endDateTime != null && order.getCreatedAt().isAfter(endDateTime)) {
                        return false;
                    }
                    return true;
                })
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .toList();
        
        // 페이징 처리
        int start = pageRequest.getPageNumber() * pageRequest.getPageSize();
        int end = Math.min(start + pageRequest.getPageSize(), filteredOrders.size());
        List<ProductOrder> pagedOrders = filteredOrders.subList(start, end);
        
        List<PurchaseOrderListResponseDto> dtoList = new ArrayList<>();
        for (ProductOrder productOrder : pagedOrders) {
            // 발주서 아이템 조회하여 요약 생성
            List<ProductOrderItem> items = productOrderItemRepository.findByProductOrderId(productOrder.getId());
            String itemsSummary = generateItemsSummary(items);
            
            // 승인 상태 조회
            String statusCodeValue = productOrder.getApprovalId().getApprovalStatus();
            if (statusCodeValue == null) {
                statusCodeValue = "PENDING";
            }
            
            // supplierName: first item's product -> supplierCompany
            String supplierName = null;
            if (!items.isEmpty()) {
                ProductOrderItem first = items.get(0);
                Product product = productRepository.findById(first.getProductId()).orElse(null);
                if (product != null && product.getSupplierCompany() != null) {
                    supplierName = product.getSupplierCompany().getCompanyName();
                }
            }

            dtoList.add(PurchaseOrderListResponseDto.builder()
                    .purchaseOrderId(productOrder.getId())
                    .purchaseOrderNumber(productOrder.getProductOrderCode())
                    .supplierName(supplierName)
                    .itemsSummary(itemsSummary)
                    .orderDate(productOrder.getCreatedAt())
                    .dueDate(productOrder.getDueDate())
                    .totalAmount(productOrder.getTotalPrice())
                    .statusCode(statusCodeValue)
                    .build());
        }
        
        return new PageImpl<>(dtoList, pageRequest, filteredOrders.size());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PurchaseOrderListResponseDto> getPurchaseOrderListBySupplier(String userId, PurchaseOrderSearchVo searchVo) {
        // 1. userId로 SupplierCompany 조회
        SupplierCompany supplierCompany = supplierCompanyRepository.findBySupplierUser_UserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자의 공급업체 정보를 찾을 수 없습니다."));

        PageRequest pageRequest = PageRequest.of(searchVo.getPage(), searchVo.getSize());

        // 날짜 범위 설정
        final LocalDateTime startDateTime = searchVo.getStartDate() != null
                ? searchVo.getStartDate().atStartOfDay()
                : null;
        final LocalDateTime endDateTime = searchVo.getEndDate() != null
                ? searchVo.getEndDate().atTime(LocalTime.MAX)
                : null;

        // 2. 모든 발주서 조회 후 필터링
        List<ProductOrder> allOrders = productOrderRepository.findAll();
        List<ProductOrder> filteredOrders = allOrders.stream()
                .filter(order -> {
                    // 2-1. 해당 공급업체의 제품만 필터링
                    List<ProductOrderItem> items = productOrderItemRepository.findByProductOrderId(order.getId());
                    if (items.isEmpty()) return false;

                    // 첫 번째 아이템의 제품이 해당 공급업체 소속인지 확인
                    ProductOrderItem first = items.get(0);
                    Product product = productRepository.findById(first.getProductId()).orElse(null);
                    if (product == null || product.getSupplierCompany() == null) return false;
                    if (!product.getSupplierCompany().getId().equals(supplierCompany.getId())) return false;

                    // 2-2. statusCode 필터링
                    String statusCode = searchVo.getStatusCode();
                    if (statusCode != null && !"ALL".equalsIgnoreCase(statusCode)) {
                        String orderStatusCode = order.getApprovalId() != null
                            ? order.getApprovalId().getApprovalStatus()
                            : "PENDING";
                        if (orderStatusCode == null) {
                            orderStatusCode = "PENDING";
                        }
                        if (!statusCode.equalsIgnoreCase(orderStatusCode)) {
                            return false;
                        }
                    }

                    // 2-3. keyword 필터링 (PurchaseOrderNumber만 가능)
                    String keyword = searchVo.getKeyword();
                    if (keyword != null && !keyword.isEmpty()) {
                        if (order.getProductOrderCode() == null || !order.getProductOrderCode().toLowerCase().contains(keyword.toLowerCase())) {
                            return false;
                        }
                    }

                    // 2-4. 날짜 범위 필터링
                    if (startDateTime != null && order.getCreatedAt().isBefore(startDateTime)) {
                        return false;
                    }
                    if (endDateTime != null && order.getCreatedAt().isAfter(endDateTime)) {
                        return false;
                    }
                    return true;
                })
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .toList();

        // 3. 페이징 처리
        int start = pageRequest.getPageNumber() * pageRequest.getPageSize();
        int end = Math.min(start + pageRequest.getPageSize(), filteredOrders.size());
        List<ProductOrder> pagedOrders = filteredOrders.subList(start, end);

        List<PurchaseOrderListResponseDto> dtoList = new ArrayList<>();
        for (ProductOrder productOrder : pagedOrders) {
            // 발주서 아이템 조회하여 요약 생성
            List<ProductOrderItem> items = productOrderItemRepository.findByProductOrderId(productOrder.getId());
            String itemsSummary = generateItemsSummary(items);

            // 승인 상태 조회
            String statusCodeValue = productOrder.getApprovalId().getApprovalStatus();
            if (statusCodeValue == null) {
                statusCodeValue = "PENDING";
            }

            // supplierName: 해당 공급업체 이름 사용
            String supplierName = supplierCompany.getCompanyName();

            dtoList.add(PurchaseOrderListResponseDto.builder()
                    .purchaseOrderId(productOrder.getId())
                    .purchaseOrderNumber(productOrder.getProductOrderCode())
                    .supplierName(supplierName)
                    .itemsSummary(itemsSummary)
                    .orderDate(productOrder.getCreatedAt())
                    .dueDate(productOrder.getDueDate())
                    .totalAmount(productOrder.getTotalPrice())
                    .statusCode(statusCodeValue)
                    .build());
        }

        return new PageImpl<>(dtoList, pageRequest, filteredOrders.size());
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseOrderDetailResponseDto getPurchaseOrderDetail(String purchaseOrderId) {
        ProductOrder productOrder = productOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new IllegalArgumentException("발주서를 찾을 수 없습니다."));
        
        List<ProductOrderItem> items = productOrderItemRepository.findByProductOrderId(purchaseOrderId);
        
        // 승인 상태 조회
        String statusCode = productOrder.getApprovalId().getApprovalStatus();
        
        List<PurchaseOrderDetailResponseDto.ItemDto> itemDtos = new ArrayList<>();
        for (ProductOrderItem item : items) {
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            itemDtos.add(PurchaseOrderDetailResponseDto.ItemDto.builder()
                    .itemId(item.getProductId())
                    .itemName(product != null ? product.getProductName() : "알 수 없는 제품")
                    .quantity(item.getCount())
                    .uomName(item.getUnit())
                    .unitPrice(item.getPrice())
                    .totalPrice(item.getPrice().multiply(item.getCount()))
                    .build());
        }

        // supplierName: first item's product -> supplierCompany
        SupplierCompany supplierCompany = null;
        if (!items.isEmpty()) {
            ProductOrderItem first = items.get(0);
            Product product = productRepository.findById(first.getProductId()).orElse(null);
            if (product != null && product.getSupplierCompany() != null) {
                supplierCompany = product.getSupplierCompany();
            }
        }
        
        return PurchaseOrderDetailResponseDto.builder()
                .statusCode(statusCode)
                .dueDate(productOrder.getDueDate())
                .purchaseOrderId(productOrder.getId())
                .purchaseOrderNumber(productOrder.getProductOrderCode())
                .orderDate(productOrder.getCreatedAt())
                .supplierId(Optional.ofNullable(supplierCompany)
                        .map(SupplierCompany::getId)
                        .orElse(null))
                .supplierName(Optional.ofNullable(supplierCompany)
                        .map(SupplierCompany::getCompanyName)
                        .orElse(null))
                .supplierNumber(Optional.ofNullable(supplierCompany)
                        .map(SupplierCompany::getCompanyCode)
                        .orElse(null))
                .managerPhone(Optional.ofNullable(supplierCompany)
                        .map(SupplierCompany::getSupplierUser)
                        .map(SupplierUser::getSupplierUserPhoneNumber)
                        .orElse(null))
                .managerEmail(Optional.ofNullable(supplierCompany)
                        .map(SupplierCompany::getSupplierUser)
                        .map(SupplierUser::getSupplierUserEmail)
                        .orElse(null))

                .items(itemDtos)
                .totalAmount(productOrder.getTotalPrice())
                .note(productOrder.getEtc())
                .build();
    }

    private String generateItemsSummary(List<ProductOrderItem> items) {
        if (items.isEmpty()) {
            return "아이템 없음";
        }

        int displayLimit = 3; // 최대 표시할 아이템 수
        List<String> names = items.stream()
                .map(item -> {
                    Product product = productRepository.findById(item.getProductId()).orElse(null);
                    String productName = product != null ? product.getProductName() : "알 수 없는 제품";
                    return productName + " " + item.getCount() + item.getUnit();
                })
                .toList();

        String result;
        if (names.size() > displayLimit) {
            result = String.join(", ", names.subList(0, displayLimit)) + ", ...";
        } else {
            result = String.join(", ", names);
        }

        return result;
    }

    @Override
    @Transactional
    public DeferredResult<ResponseEntity<ApiResponse<Void>>> approvePurchaseOrderAsync(String purchaseOrderId, String requesterId) {
        log.info("구매주문 승인 시작 - purchaseOrderId: {}, requesterId: {}", purchaseOrderId, requesterId);

        // DeferredResult 생성 (타임아웃 30초)
        DeferredResult<ResponseEntity<ApiResponse<Void>>> deferredResult = new DeferredResult<>(30000L);

        // 타임아웃 처리
        deferredResult.onTimeout(() -> {
            deferredResult.setResult(ResponseEntity
                    .status(HttpStatus.REQUEST_TIMEOUT)
                    .body(ApiResponse.fail("처리 시간이 초과되었습니다.", HttpStatus.REQUEST_TIMEOUT)));
        });

        try {
            // 1. ProductOrder 조회
            ProductOrder productOrder = productOrderRepository.findById(purchaseOrderId)
                    .orElseThrow(() -> new RuntimeException("발주서를 찾을 수 없습니다: " + purchaseOrderId));

            ProductOrderApproval approval = productOrder.getApprovalId();
            if (approval == null) {
                throw new RuntimeException("승인 정보가 없습니다.");
            }

            // 2. 승인 상태 업데이트
            ProductOrderApproval updatedApproval = approval.toBuilder()
                    .approvalStatus("APPROVAL")
                    .approvedAt(LocalDateTime.now())
                    .approvedBy(requesterId)
                    .build();

            productOrderApprovalRepository.save(updatedApproval);

            //TODO 알람 필요 - 발주서 승인 완료 알림 (공급업체)

            // 3. MRP Run 상태 업데이트 (mrpRunId가 있는 경우만)
            List<ProductOrderItem> items = productOrderItemRepository.findByProductOrderId(purchaseOrderId);
            for (ProductOrderItem item : items) {
                if (item.getMrpRunId() != null) {
                    org.ever._4ever_be_scm.scm.pp.entity.MrpRun mrpRun = mrpRunRepository.findById(item.getMrpRunId())
                        .orElseThrow(() -> new RuntimeException("MRP Run을 찾을 수 없습니다: " + item.getMrpRunId()));
                    mrpRun.setStatus("ORDER_APPROVED");  // 발주서 승인됨
                    mrpRunRepository.save(mrpRun);
                }
            }

            // 4. Supplier Company ID 추출
            String supplierCompanyId = null;
            if (!items.isEmpty()) {
                ProductOrderItem firstItem = items.get(0);
                Product product = productRepository.findById(firstItem.getProductId()).orElse(null);
                if (product != null && product.getSupplierCompany() != null) {
                    supplierCompanyId = product.getSupplierCompany().getId();
                }
            }

            if (supplierCompanyId == null) {
                throw new RuntimeException("공급업체 정보를 찾을 수 없습니다.");
            }

            // 5. 트랜잭션 ID 생성 및 DeferredResult 등록
            String transactionId = UUID.randomUUID().toString();
            asyncResultManager.registerResult(transactionId, deferredResult);

            // 6. PurchaseOrderApprovalEvent 발행
            PurchaseOrderApprovalEvent event = PurchaseOrderApprovalEvent.builder()
                    .transactionId(transactionId)
                    .purchaseOrderId(productOrder.getId())
                    .purchaseOrderNumber(productOrder.getProductOrderCode())
                    .supplierCompanyId(supplierCompanyId)
                    .totalAmount(productOrder.getTotalPrice())
                    .dueDate(productOrder.getDueDate())
                    .memo(productOrder.getEtc())
                    .timestamp(System.currentTimeMillis())
                    .build();

            kafkaProducerService.sendToTopic(
                    KafkaTopicConfig.PURCHASE_ORDER_APPROVAL_TOPIC,
                    purchaseOrderId, event);

            log.info("구매주문 승인 이벤트 발행 완료 - transactionId: {}, purchaseOrderId: {}",
                    transactionId, purchaseOrderId);

        } catch (Exception e) {
            log.error("구매주문 승인 실패 - purchaseOrderId: {}, error: {}", purchaseOrderId, e.getMessage(), e);
            deferredResult.setResult(ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("구매주문 승인 실패: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR)));
        }

        return deferredResult;
    }

    @Override
    @Transactional
    public void rejectPurchaseOrder(String purchaseOrderId, String requesterId, String reason) {
        ProductOrder productOrder = productOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new RuntimeException("발주서를 찾을 수 없습니다: " + purchaseOrderId));
        
        ProductOrderApproval approval = productOrder.getApprovalId();
        if (approval == null) {
            throw new RuntimeException("승인 정보가 없습니다.");
        }


        ProductOrderApproval updatedApproval = approval.toBuilder()
                .approvalStatus("REJECTED")
                .approvedAt(LocalDateTime.now())
                .rejectedReason(reason)
                .approvedBy(requesterId)
                .build();

        productOrderApprovalRepository.save(updatedApproval);

        //TODO 알람 필요 - 발주서 반려 알림 (요청자, 사유 포함)
    }

    @Override
    @Transactional
    public void startDelivery(String purchaseOrderId) {
        ProductOrder productOrder = productOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new RuntimeException("발주서를 찾을 수 없습니다: " + purchaseOrderId));

        // 1. Shipment 생성
        org.ever._4ever_be_scm.scm.mm.entity.ProductOrderShipment shipment =
                org.ever._4ever_be_scm.scm.mm.entity.ProductOrderShipment.builder()
                .status("DELIVERING")  // 배송중
                .expectedDelivery(productOrder.getDueDate() != null ? productOrder.getDueDate().toLocalDate() : null)
                .build();

        productOrderShipmentRepository.save(shipment);

        productOrder.setShipmentId(shipment);
        productOrderRepository.save(productOrder);

        // 1-1. Approval 상태 업데이트
        if (productOrder.getApprovalId() != null) {
            ProductOrderApproval approval = productOrder.getApprovalId();
            approval = approval.toBuilder()
                    .approvalStatus("DELIVERING")
                    .build();
            productOrderApprovalRepository.save(approval);
            log.info("발주서 승인 상태 업데이트 - purchaseOrderId: {}, status: DELIVERING", purchaseOrderId);
        }

        //TODO 알람 필요 - 배송 시작 알림 (발주서 요청자)

        // 2. MRP Run 상태 업데이트 (mrpRunId가 있는 경우만)
        List<ProductOrderItem> items = productOrderItemRepository.findByProductOrderId(purchaseOrderId);
        for (ProductOrderItem item : items) {
            if (item.getMrpRunId() != null) {
                org.ever._4ever_be_scm.scm.pp.entity.MrpRun mrpRun = mrpRunRepository.findById(item.getMrpRunId())
                    .orElseThrow(() -> new RuntimeException("MRP Run을 찾을 수 없습니다: " + item.getMrpRunId()));
                mrpRun.setStatus("DELIVERING");  // 배송중
                mrpRunRepository.save(mrpRun);
            }
        }

        // 3. 자동 입고 완료 스케줄링
        // Supplier의 deliveryDays를 조회하여 자동으로 입고 완료 처리 예약
        scheduleAutoDeliveryCompletion(productOrder);
    }

    /**
     * 자동 입고 완료 스케줄링
     * Supplier의 deliveryDays 만큼 지연 후 자동으로 completeDelivery 호출
     */
    private void scheduleAutoDeliveryCompletion(ProductOrder productOrder) {
        try {
            // SupplierCompany 조회 (발주서의 supplierCompanyName으로 조회)
            SupplierCompany supplierCompany = supplierCompanyRepository
                    .findByCompanyName(productOrder.getSupplierCompanyName())
                    .orElse(null);

            if (supplierCompany == null) {
                log.warn("공급업체 정보를 찾을 수 없어 자동 입고 완료를 예약하지 않습니다. - supplierCompanyName: {}",
                        productOrder.getSupplierCompanyName());
                return;
            }

            if (supplierCompany.getDeliveryDays() == null) {
                log.warn("공급업체의 배송 소요 기간이 설정되지 않아 자동 입고 완료를 예약하지 않습니다. - supplierCompany: {}",
                        supplierCompany.getCompanyName());
                return;
            }

            // DeliverySchedulerService를 통해 자동 완료 예약
            deliverySchedulerService.scheduleDeliveryCompletion(
                    productOrder.getId(),
                    supplierCompany.getDeliveryDays()
            );

            log.info("자동 입고 완료 예약 완료 - purchaseOrderId: {}, deliveryDays: {}일",
                    productOrder.getId(),
                    supplierCompany.getDeliveryDays().toDays());

        } catch (Exception e) {
            log.error("자동 입고 완료 예약 중 오류 발생 - purchaseOrderId: {}, error: {}",
                    productOrder.getId(), e.getMessage(), e);
            // 스케줄링 실패해도 배송 시작은 정상 처리됨
        }
    }

    @Override
    @Transactional
    public void completeDelivery(String purchaseOrderId) {
        ProductOrder productOrder = productOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new RuntimeException("발주서를 찾을 수 없습니다: " + purchaseOrderId));

        // 1. Shipment 업데이트
        if (productOrder.getShipmentId() == null) {
            throw new RuntimeException("배송 정보가 없습니다.");
        }

        org.ever._4ever_be_scm.scm.mm.entity.ProductOrderShipment shipment = productOrder.getShipmentId();
        shipment.setStatus("DELIVERED");
        shipment.setDeliveredAt(java.time.LocalDate.now());
        productOrderShipmentRepository.save(shipment);

        // 1-1. Approval 상태 업데이트
        if (productOrder.getApprovalId() != null) {
            ProductOrderApproval approval = productOrder.getApprovalId();
            approval = approval.toBuilder()
                    .approvalStatus("DELIVERED")
                    .build();
            productOrderApprovalRepository.save(approval);
            log.info("발주서 승인 상태 업데이트 - purchaseOrderId: {}, status: DELIVERED", purchaseOrderId);
        }

        //TODO 알람 필요 - 입고 완료 알림 (담당자)

        // 2. 재고 증가 및 MRP Run 상태 업데이트
        List<ProductOrderItem> items = productOrderItemRepository.findByProductOrderId(purchaseOrderId);
        for (ProductOrderItem item : items) {
            // 2-1. 재고 증가
            Product product = productRepository.findById(item.getProductId())
                .orElseThrow(() -> new RuntimeException("제품을 찾을 수 없습니다: " + item.getProductId()));

            ProductStock stock = productStockRepository
                .findByProductId(item.getProductId())
                .orElse(ProductStock.builder()
                    .product(product)
                    .availableCount(java.math.BigDecimal.ZERO)
                    .reservedCount(java.math.BigDecimal.ZERO)
                    .safetyCount(java.math.BigDecimal.ZERO)
                    .build());

            // 2-2. MRP Run 상태 업데이트 및 재고 예약 처리 (mrpRunId가 있는 경우만)
            if (item.getMrpRunId() != null) {
                // MRP Run 상태 업데이트
                org.ever._4ever_be_scm.scm.pp.entity.MrpRun mrpRun = mrpRunRepository.findById(item.getMrpRunId())
                    .orElseThrow(() -> new RuntimeException("MRP Run을 찾을 수 없습니다: " + item.getMrpRunId()));
                mrpRun.setStatus("DELIVERED");
                mrpRunRepository.save(mrpRun);

                //TODO 입출고처리로 변경완료
                // MRP 기반 구매: availableCount와 reservedCount 모두 증가
                // 이렇게 하면 해당 견적을 위해 자동으로 예약되어 다른 견적이 사용할 수 없음
                stockTransferService.processStockDelivery(
                        item.getProductId(),
                        item.getCount(),
                        "system", // requesterId
                        productOrder.getProductOrderCode(), // referenceCode
                        "발주서 입고 (MRP)" // reason
                );
                // 재고 다시 조회 후 예약 처리
                stock = productStockRepository.findByProductId(item.getProductId())
                        .orElseThrow(() -> new RuntimeException("재고를 찾을 수 없습니다."));
                stock.reserveStock(item.getCount());  // reservedCount도 증가
                productStockRepository.save(stock);

                // MRP 상태 업데이트: 현재 MRP Run이 부족량을 충족하는지 확인
                if (mrpRun.getMrpId() != null) {
                    org.ever._4ever_be_scm.scm.pp.entity.Mrp mrp = mrpRepository.findById(mrpRun.getMrpId())
                        .orElse(null);

                    if (mrp != null) {
                        java.math.BigDecimal shortage = mrp.getShortageQuantity() != null
                            ? mrp.getShortageQuantity() : java.math.BigDecimal.ZERO;

                        // 현재 MRP Run의 입고량이 부족량 이상이면 SUFFICIENT로 변경
                        if (mrpRun.getQuantity().compareTo(shortage) >= 0) {
                            mrp.setStatus("SUFFICIENT");
                            mrp.setShortageQuantity(java.math.BigDecimal.ZERO);  // 부족량 해소
                            mrpRepository.save(mrp);
                        }
                    }
                }
            } else {
                //TODO 입출고처리로 변경완료
                // 일반 구매 (MRP 아님): availableCount만 증가
                stockTransferService.processStockDelivery(
                        item.getProductId(),
                        item.getCount(),
                        "system", // requesterId
                        productOrder.getProductOrderCode(), // referenceCode
                        "발주서 입고 (일반)" // reason
                );
            }
        }
    }
}
