package org.ever._4ever_be_scm.scm.pp.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ever._4ever_be_scm.scm.iv.entity.Product;
import org.ever._4ever_be_scm.scm.iv.entity.ProductStock;
import org.ever._4ever_be_scm.scm.iv.entity.ProductStockLog;
import org.ever._4ever_be_scm.scm.iv.entity.SupplierCompany;
import org.ever._4ever_be_scm.scm.iv.entity.SupplierUser;
import org.ever._4ever_be_scm.scm.iv.entity.Warehouse;
import org.ever._4ever_be_scm.scm.iv.repository.ProductRepository;
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
import org.ever._4ever_be_scm.scm.pp.repository.MesRepository;
import org.ever._4ever_be_scm.scm.pp.service.DashboardService;
import org.ever._4ever_be_scm.scm.pp.service.dto.DashboardWorkflowItemDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final int DEFAULT_SIZE = 5;
    private static final int MAX_SIZE = 20;
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
    private final MesRepository mesRepository;
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // 특정 공급사의 발주서 조회
    @Override
    @Transactional(readOnly = true)
    public List<DashboardWorkflowItemDto> getSupplierPurchaseOrders(String userId, int size) {
        int limit = normalizeSize(size);

        // supplier_user에서 user_id로 supplier_user table의 id 조회
        SupplierUser supplierUser = supplierUserRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 userId와 일치하는 공급사 사용자를 찾을 수 없습니다."));

        // supplier_company의 supplier_user_id를 통해 공급사 엔티티 조회
        SupplierCompany supplierCompany = supplierCompanyRepository.findBySupplierUser(supplierUser)
                .orElseThrow(() -> new IllegalArgumentException("해당 공급사 사용자와 연결된 공급사가 없습니다."));
        String supplierCompanyName = supplierCompany.getCompanyName();
        log.info("[INFO][SUP] 조회한 공급사의 이름: {}", supplierCompanyName);

        // itemTitle용 제품 이름 조회
        // product에 supplier_company_id가있음.
        // 공급사 ID
        String supplierCompanyId = supplierCompany.getId();
        Product product = productRepository.findFirstBySupplierCompany_Id(supplierCompanyId);
        String productTitle = product != null ? product.getProductName() : supplierCompanyName;

        // 발주서 테이블(product_order)의 공급사 이름으로 조회하여 공급사에게 할당된 발주서 목록 조회
        List<ProductOrder> orders = productOrderRepository
                .findBySupplierCompanyNameOrderByCreatedAtDesc(supplierCompanyName)
                .stream()
                .limit(limit)
                .toList();

        if (orders.isEmpty()) {
            log.info("[DASHBOARD][MOCK][SUPPLIER][PO] 실데이터 없음 - 목업 발주서 반환, userId: {}", userId);
            return buildMockSupplierPurchaseOrders(limit, supplierCompanyName, productTitle);
        }

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
        int limit = normalizeSize(size);

        return productRequestRepository
                .findByRequesterIdOrderByCreatedAtDesc(userId)
                .stream()
                .limit(limit)
                .map(this::toPurchaseRequestItem)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DashboardWorkflowItemDto> getPurchaseRequestsOverall(int size) {
        int limit = normalizeSize(size);

        var requests = productRequestRepository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .limit(limit)
                .map(this::toPurchaseRequestItem)
                .toList();

        if (requests.isEmpty()) {
            log.info("[DASHBOARD][MOCK][MM][PO] 실데이터 없음 - 전체 구매요청 목업 데이터 반환");
            return buildMockPurchaseRequests(limit);
        }

        return requests;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DashboardWorkflowItemDto> getMmPurchaseOrders(int size) {
        int limit = normalizeSize(size);

        var orders = productOrderRepository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .limit(limit)
                .map(order -> DashboardWorkflowItemDto.builder()
                        .itemId(order.getId())
                        .itemTitle(order.getSupplierCompanyName())
                        .itemNumber(order.getProductOrderCode())
                        .name(order.getRequesterId())
                        .statusCode(resolveOrderStatus(order))
                        .date(formatDate(order.getCreatedAt()))
                        .build())
                .toList();

        if (orders.isEmpty()) {
            log.info("[DASHBOARD][MOCK][MM][SO] 실데이터 없음 - 전체 발주서 목업 데이터 반환");
            return buildMockPurchaseOrders(limit);
        }

        return orders;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DashboardWorkflowItemDto> getInboundDeliveries(String userId, int size) {
        int limit = normalizeSize(size);

        List<ProductStockLog> inboundLogs = productStockLogRepository
                .findByMovementTypeOrderByCreatedAtDesc(MOVEMENT_TYPE_INBOUND)
                .stream()
                .limit(limit)
                .toList();

        if (inboundLogs.isEmpty()) {
            log.info("[DASHBOARD][MOCK][IM][IN] 실데이터 없음 - 입고 목업 데이터 반환");
            return buildMockStockLogs(limit, MOVEMENT_TYPE_INBOUND);
        }

        return inboundLogs.stream()
                .map(this::toStockLogItem)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DashboardWorkflowItemDto> getOutboundDeliveries(String userId, int size) {
        int limit = normalizeSize(size);

        List<ProductStockLog> outboundLogs = productStockLogRepository
                .findByMovementTypeOrderByCreatedAtDesc(MOVEMENT_TYPE_OUTBOUND)
                .stream()
                .limit(limit)
                .toList();

        if (outboundLogs.isEmpty()) {
            log.info("[DASHBOARD][MOCK][IM][OUT] 실데이터 없음 - 출고 목업 데이터 반환");
            return buildMockStockLogs(limit, MOVEMENT_TYPE_OUTBOUND);
        }

        return outboundLogs.stream()
                .map(this::toStockLogItem)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DashboardWorkflowItemDto> getQuotationsToProduction(String userId, int size) {
        int limit = normalizeSize(size);

        BusinessQuotationListResponseDto response =
                businessQuotationServicePort.getQuotationList("APPROVAL", null, LocalDate.now().minusMonths(1), LocalDate.now(), 0, limit);

        List<DashboardWorkflowItemDto> items = response.getContent().stream()
                .map(this::toQuotationItem)
                .toList();

        if (items.isEmpty()) {
            log.info("[DASHBOARD][MOCK][PP][QT] 실데이터 없음 - 생산 전환 견적 목업 데이터 반환");
            return buildMockProductionQuotations(limit);
        }

        return items;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DashboardWorkflowItemDto> getProductionInProgress(String userId, int size) {
        int limit = normalizeSize(size);

        List<DashboardWorkflowItemDto> items = mesRepository.findByStatusOrderByCreatedAtDesc("IN_PROGRESS")
                .stream()
                .limit(limit)
                .map(this::toMesItem)
                .toList();

        if (items.isEmpty()) {
            log.info("[DASHBOARD][MOCK][PP][MES] 실데이터 없음 - MES 작업 목업 데이터 반환");
            return buildMockMesJobs(limit);
        }

        return items;
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

    private DashboardWorkflowItemDto toMesItem(org.ever._4ever_be_scm.scm.pp.entity.Mes mes) {
        String productName = Optional.ofNullable(mes.getProductId())
                .flatMap(productRepository::findById)
                .map(Product::getProductName)
                .orElse("생산 작업");

        String title = productName + " · MES 작업";

        LocalDateTime startDateTime = mes.getStartDate() != null
                ? mes.getStartDate().atStartOfDay()
                : mes.getCreatedAt();

        return DashboardWorkflowItemDto.builder()
                .itemId(mes.getId())
                .itemTitle(title)
                .itemNumber(mes.getMesNumber())
                .name(mes.getQuotationId())
                .statusCode(Optional.ofNullable(mes.getStatus()).orElse("IN_PROGRESS"))
                .date(startDateTime != null ? startDateTime.format(ISO_FORMATTER) : null)
                .build();
    }

    private List<DashboardWorkflowItemDto> buildMockPurchaseRequests(int size) {
        int itemCount = Math.min(size > 0 ? size : DEFAULT_SIZE, DEFAULT_SIZE);

        return IntStream.range(0, itemCount)
                .mapToObj(i -> DashboardWorkflowItemDto.builder()
                        .itemId(UUID.randomUUID().toString())
                        .itemTitle("긴급 구매요청 " + (i + 1))
                        .itemNumber(String.format("REQ-MOCK-%04d", i + 1))
                        .name("요청자 " + (i + 1))
                        .statusCode(i % 2 == 0 ? "REQUESTED" : "APPROVED")
                        .date(LocalDate.now().minusDays(i).toString())
                        .build())
                .toList();
    }

    private List<DashboardWorkflowItemDto> buildMockPurchaseOrders(int size) {
        int itemCount = Math.min(size > 0 ? size : DEFAULT_SIZE, DEFAULT_SIZE);

        return IntStream.range(0, itemCount)
                .mapToObj(i -> DashboardWorkflowItemDto.builder()
                        .itemId(UUID.randomUUID().toString())
                        .itemTitle("예비부품 발주 " + (i + 1))
                        .itemNumber(String.format("PO-MOCK-%04d", i + 1))
                        .name("담당자 " + (i + 1))
                        .statusCode(i % 2 == 0 ? "APPROVED" : "IN_PROGRESS")
                        .date(LocalDate.now().minusDays(i).toString())
                        .build())
                .toList();
    }

    private List<DashboardWorkflowItemDto> buildMockSupplierPurchaseOrders(int size, String supplierCompanyName, String productTitle) {
        int itemCount = Math.min(size > 0 ? size : DEFAULT_SIZE, DEFAULT_SIZE);
        String titleBase = (productTitle != null ? productTitle : "공급사 제품") + " 발주";
        String nameBase = supplierCompanyName != null ? supplierCompanyName : "공급사 담당자";

        return IntStream.range(0, itemCount)
                .mapToObj(i -> DashboardWorkflowItemDto.builder()
                        .itemId(UUID.randomUUID().toString())
                        .itemTitle(titleBase)
                        .itemNumber(String.format("PO-MOCK-%04d", i + 1))
                        .name(nameBase)
                        .statusCode(i % 2 == 0 ? "REQUESTED" : "APPROVED")
                        .date(LocalDate.now().minusDays(i).toString())
                        .build())
                .toList();
    }

    private List<DashboardWorkflowItemDto> buildMockStockLogs(int size, String movementType) {
        int itemCount = Math.min(size > 0 ? size : DEFAULT_SIZE, DEFAULT_SIZE);
        boolean inbound = MOVEMENT_TYPE_INBOUND.equals(movementType);

        return IntStream.range(0, itemCount)
                .mapToObj(i -> DashboardWorkflowItemDto.builder()
                        .itemId(UUID.randomUUID().toString())
                        .itemTitle(String.format("창고 A · %s", inbound ? "입고" : "출고"))
                        .itemNumber(String.format("%s-MOCK-%04d", inbound ? "IN" : "OUT", i + 1))
                        .name("재고 담당자 " + (i + 1))
                        .statusCode(inbound ? "COMPLETED" : (i % 2 == 0 ? "IN_PROGRESS" : "COMPLETED"))
                        .date(LocalDate.now().minusDays(i).toString())
                        .build())
                .toList();
    }

    private List<DashboardWorkflowItemDto> buildMockProductionQuotations(int size) {
        int itemCount = Math.min(size > 0 ? size : DEFAULT_SIZE, DEFAULT_SIZE);

        return IntStream.range(0, itemCount)
                .mapToObj(i -> DashboardWorkflowItemDto.builder()
                        .itemId(UUID.randomUUID().toString())
                        .itemTitle("생산 전환 견적 목업 " + (i + 1))
                        .itemNumber(String.format("QT-MOCK-%04d", i + 1))
                        .name("생산 담당자 " + (i + 1))
                        .statusCode(i % 2 == 0 ? "APPROVED" : "PENDING")
                        .date(LocalDate.now().minusDays(i).toString())
                        .build())
                .toList();
    }

    private List<DashboardWorkflowItemDto> buildMockMesJobs(int size) {
        int itemCount = Math.min(size > 0 ? size : DEFAULT_SIZE, DEFAULT_SIZE);

        return IntStream.range(0, itemCount)
                .mapToObj(i -> DashboardWorkflowItemDto.builder()
                        .itemId(UUID.randomUUID().toString())
                        .itemTitle("MES 작업 목업 " + (i + 1))
                        .itemNumber(String.format("MES-MOCK-%04d", i + 1))
                        .name("라인 " + (i + 1))
                        .statusCode(i % 2 == 0 ? "IN_PROGRESS" : "READY")
                        .date(LocalDate.now().minusDays(i).toString())
                        .build())
                .toList();
    }

    private int normalizeSize(int size) {
        int candidate = size > 0 ? size : DEFAULT_SIZE;
        return Math.min(candidate, MAX_SIZE);
    }
}
