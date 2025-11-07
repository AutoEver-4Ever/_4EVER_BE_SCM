package org.ever._4ever_be_scm.scm.pp.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ever._4ever_be_scm.scm.iv.entity.Product;
import org.ever._4ever_be_scm.scm.iv.entity.ProductStock;
import org.ever._4ever_be_scm.scm.iv.entity.ProductStockLog;
import org.ever._4ever_be_scm.scm.iv.entity.SupplierCompany;
import org.ever._4ever_be_scm.scm.iv.entity.SupplierUser;
import org.ever._4ever_be_scm.scm.iv.entity.Warehouse;
import org.ever._4ever_be_scm.scm.iv.repository.ProductStockLogRepository;
import org.ever._4ever_be_scm.scm.iv.repository.SupplierCompanyRepository;
import org.ever._4ever_be_scm.scm.iv.repository.SupplierUserRepository;
import org.ever._4ever_be_scm.scm.mm.entity.ProductOrder;
import org.ever._4ever_be_scm.scm.mm.entity.ProductOrderApproval;
import org.ever._4ever_be_scm.scm.mm.entity.ProductRequest;
import org.ever._4ever_be_scm.scm.mm.repository.ProductOrderApprovalRepository;
import org.ever._4ever_be_scm.scm.mm.repository.ProductOrderRepository;
import org.ever._4ever_be_scm.scm.mm.repository.ProductRequestRepository;
import org.ever._4ever_be_scm.scm.pp.integration.dto.BusinessQuotationDto;
import org.ever._4ever_be_scm.scm.pp.integration.dto.BusinessQuotationListResponseDto;
import org.ever._4ever_be_scm.scm.pp.integration.port.BusinessQuotationServicePort;
import org.ever._4ever_be_scm.scm.pp.service.DashboardService;
import org.ever._4ever_be_scm.scm.pp.service.dto.DashboardWorkflowItemDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final int DEFAULT_SIZE = 5;
    private static final String DEFAULT_STATUS = "PENDING";
    private static final String DEFAULT_STOCK_STATUS = "UNKNOWN";
    private static final String MOVEMENT_TYPE_INBOUND = "입고";
    private static final String MOVEMENT_TYPE_OUTBOUND = "출고";
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final ProductOrderRepository productOrderRepository;
    private final ProductRequestRepository productRequestRepository;
    private final SupplierUserRepository supplierUserRepository;
    private final SupplierCompanyRepository supplierCompanyRepository;
    private final ProductRepository productRepository;
    private final ProductOrderApprovalRepository productOrderApprovalRepository;
    private final ProductStockLogRepository productStockLogRepository;
    private final BusinessQuotationServicePort businessQuotationServicePort;
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // 특정 공급사의 발주서 조회
    @Override
    @Transactional(readOnly = true)
    public List<DashboardWorkflowItemDto> getSupplierPurchaseOrders(String userId, int size) {
        int limit = size > 0 ? size : DEFAULT_SIZE;

        // supplier_user에서 user_id로 supplier_user table의 id 조회
        SupplierUser supplierUser = supplierUserRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 userId와 일치하는 사용자가 없습니다."));
        String supplierUserId = supplierUser.getId();

        // supplier_company의 supplier_user_id를 조회하여 공급사의 이름(supplier_company_name)을 조회
        SupplierCompany supplierCompany = supplierCompanyRepository.findByCompanyName(supplierUserId)
                .orElseThrow(() -> new IllegalArgumentException("해당 공급사의 담당자와 연결된 공급사가 없습니다."));
        String supplierCompanyName = supplierCompany.getCompanyName();
        log.info("[INFO][SUP] 조회한 공급사의 이름: {}", supplierCompanyName);

        // itemTitle용 제품 이름 조회
        // product에 supplier_company_id가있음.
        // 공급사 ID
        String supplierCompanyId = supplierCompany.getId();
        Product product = productRepository.getProductNameBySupplierCompanyId(supplierCompanyId);
        String productTitle = product != null ? product.getProductName() : supplierCompanyName;

        // 발주서 테이블(product_order)의 공급사 이름으로 조회하여 공급사에게 할당된 발주서 목록 조회
        List<ProductOrder> orders = productOrderRepository
                .findBySupplierCompanyNameOrderByCreatedAtDesc(
                        supplierCompanyName,
                        PageRequest.of(0, size > 0 ? size : 5)
                )
                .getContent();

        return orders.stream()
                .map(order -> DashboardWorkflowItemDto.builder()
                        .itemId(order.getId())
                        .itemTitle(productTitle + " 발주")
                        .itemNumber(order.getProductOrderCode())
                        .name(supplierCompanyName)
                        .statusCode(Optional.ofNullable(order.getApprovalId())
                                .map(ProductOrderApproval::getApprovalStatus)
                                .orElse("PENDING"))
                        .date(order.getCreatedAt() != null ?
                                order.getCreatedAt().format(formatter) : null)
                        .build())
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public List<DashboardWorkflowItemDto> getPurchaseRequests(String userId, int size) {
        int limit = size > 0 ? size : DEFAULT_SIZE;

        return productRequestRepository
                .findByRequesterIdOrderByCreatedAtDesc(userId, PageRequest.of(0, limit))
                .stream()
                .map(this::toPurchaseRequestItem)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DashboardWorkflowItemDto> getMmPurchaseOrders(String userId, int size) {
        int limit = size > 0 ? size : DEFAULT_SIZE;

        return productOrderRepository
                .findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit))
                .stream()
                .map(order -> DashboardWorkflowItemDto.builder()
                        .itemId(order.getId())
                        .itemTitle(order.getSupplierCompanyName())
                        .itemNumber(order.getProductOrderCode())
                        .name(order.getRequesterId())
                        .statusCode(resolveOrderStatus(order))
                        .date(formatDate(order.getCreatedAt()))
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DashboardWorkflowItemDto> getInboundDeliveries(String userId, int size) {
        int limit = size > 0 ? size : DEFAULT_SIZE;

        List<ProductStockLog> inboundLogs = productStockLogRepository
                .findByMovementTypeOrderByCreatedAtDesc(MOVEMENT_TYPE_INBOUND, PageRequest.of(0, limit));

        return inboundLogs.stream()
                .map(this::toStockLogItem)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DashboardWorkflowItemDto> getOutboundDeliveries(String userId, int size) {
        int limit = size > 0 ? size : DEFAULT_SIZE;

        List<ProductStockLog> outboundLogs = productStockLogRepository
                .findByMovementTypeOrderByCreatedAtDesc(MOVEMENT_TYPE_OUTBOUND, PageRequest.of(0, limit));

        return outboundLogs.stream()
                .map(this::toStockLogItem)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DashboardWorkflowItemDto> getQuotationsToProduction(String userId, int size) {
        int limit = size > 0 ? size : DEFAULT_SIZE;

        BusinessQuotationListResponseDto response =
                businessQuotationServicePort.getQuotationList("APPROVAL", LocalDate.now().minusMonths(1), LocalDate.now(), 0, limit);

        return response.getContent().stream()
                .map(this::toQuotationItem)
                .toList();
    }

    private DashboardWorkflowItemDto toPurchaseRequestItem(ProductRequest request) {
        return DashboardWorkflowItemDto.builder()
                .itemId(request.getId())
                .itemTitle(request.getProductRequestType())
                .itemNumber(request.getProductRequestCode())
                .name(request.getRequesterId())
                .statusCode(resolveRequestStatus(request))
                .date(formatDate(request.getCreatedAt()))
                .build();
    }

    private String resolveOrderStatus(ProductOrder order) {
        return Optional.ofNullable(order.getApprovalId())
                .map(approval -> Optional.ofNullable(approval.getApprovalStatus()).orElse(DEFAULT_STATUS))
                .orElse(DEFAULT_STATUS);
    }

    private String resolveRequestStatus(ProductRequest request) {
        return Optional.ofNullable(request.getApprovalId())
                .map(approval -> Optional.ofNullable(approval.getApprovalStatus()).orElse(DEFAULT_STATUS))
                .orElse(DEFAULT_STATUS);
    }

    private String formatDate(java.time.LocalDateTime datetime) {
        return datetime != null ? datetime.format(ISO_FORMATTER) : null;
    }

    private DashboardWorkflowItemDto toStockLogItem(ProductStockLog stockLog) {
        ProductStock productStock = stockLog.getProductStock();
        Product product = productStock != null ? productStock.getProduct() : null;
        Warehouse warehouse = productStock != null ? productStock.getWarehouse() : null;

        String itemTitle = product != null ? product.getProductName() : "입고 처리";
        String itemNumber = Optional.ofNullable(stockLog.getReferenceCode()).orElse(stockLog.getId());
        String warehouseName = warehouse != null ? warehouse.getWarehouseName() : null;
        String statusCode = productStock != null && productStock.getStatus() != null
                ? productStock.getStatus()
                : DEFAULT_STOCK_STATUS;

        return DashboardWorkflowItemDto.builder()
                .itemId(stockLog.getId())
                .itemTitle(itemTitle)
                .itemNumber(itemNumber)
                .name(warehouseName)
                .statusCode(statusCode)
                .date(formatDate(stockLog.getCreatedAt()))
                .build();
    }

    private DashboardWorkflowItemDto toQuotationItem(BusinessQuotationDto quotation) {
        String quotationId = quotation.getQuotationId();
        String quotationNumber = quotation.getQuotationNumber();
        String customerName = quotation.getCustomerName();
        String statusCode = quotation.getStatusCode();
        String dueDate = quotation.getDueDate();

        String itemTitle = customerName + " · 생산 전환 견적";

        return DashboardWorkflowItemDto.builder()
                .itemId(quotationId)
                .itemTitle(itemTitle)
                .itemNumber(quotationNumber)
                .name(customerName)
                .statusCode(statusCode != null ? statusCode : DEFAULT_STATUS)
                .date(dueDate)
                .build();
    }
}
