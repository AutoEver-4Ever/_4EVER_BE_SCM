package org.ever._4ever_be_scm.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ever._4ever_be_scm.scm.iv.entity.*;
import org.ever._4ever_be_scm.scm.iv.repository.*;
import org.ever._4ever_be_scm.scm.mm.entity.*;
import org.ever._4ever_be_scm.scm.mm.repository.*;
import org.ever._4ever_be_scm.scm.pp.entity.*;
import org.ever._4ever_be_scm.scm.pp.repository.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 전체 도메인 목업 데이터 초기화 컴포넌트
 * dev, local 프로파일에서만 실행됩니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Profile({"dev", "local"})
public class MockDataInitializer {

    // IV 도메인 Repository
    private final SupplierCompanyRepository supplierCompanyRepository;
    private final SupplierUserRepository supplierUserRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final ProductStockRepository productStockRepository;
    private final ProductStockLogRepository productStockLogRepository;

    // MM 도메인 Repository
    private final ProductOrderApprovalRepository productOrderApprovalRepository;
    private final ProductOrderRepository productOrderRepository;
    private final ProductOrderItemRepository productOrderItemRepository;
    private final ProductOrderShipmentRepository productOrderShipmentRepository;
    private final ProductRequestApprovalRepository productRequestApprovalRepository;
    private final ProductRequestRepository productRequestRepository;
    private final ProductRequestItemRepository productRequestItemRepository;

    // PP 도메인 Repository
    private final BomRepository bomRepository;
    private final BomExplosionRepository bomExplosionRepository;
    private final BomItemRepository bomItemRepository;
    private final MpsRepository mpsRepository;
    private final MpsDetailRepository mpsDetailRepository;
    private final MrpRepository mrpRepository;
    private final MrpRunRepository mrpRunRepository;
    private final OperationRepository operationRepository;
    private final RoutingRepository routingRepository;

    @PostConstruct
    @Transactional
    public void initializeMockData() {
        log.info("=== 목업 데이터 초기화 시작 ===");
        
        try {
            // 기존 데이터 체크 (중복 방지)
            if (supplierCompanyRepository.count() > 0) {
                log.info("목업 데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
                return;
            }

            // 1. IV 도메인 데이터 생성
            initializeIvDomain();
            
            // 2. MM 도메인 데이터 생성
            initializeMmDomain();
            
            // 3. PP 도메인 데이터 생성
            initializePpDomain();
            
            log.info("=== 목업 데이터 초기화 완료 ===");
        } catch (Exception e) {
            log.error("목업 데이터 초기화 중 오류 발생", e);
        }
    }

    /**
     * IV 도메인 목업 데이터 생성
     */
    private void initializeIvDomain() {
        log.info("IV 도메인 목업 데이터 생성 시작");

        // 1. SupplierCompany 생성
        List<SupplierCompany> companies = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            SupplierCompany company = SupplierCompany.builder()
                    .companyCode("SUP" + String.format("%03d", i))
                    .companyName("공급업체" + i)
                    .category(i % 2 == 1 ? "ITEM" : "MATERIAL")
                    .baseAddress("서울시 강남구")
                    .detailAddress("테헤란로 " + (100 + i) + "길")
                    .status("ACTIVE")
                    .officePhone("02-1234-567" + i)
                    .deliveryDays(3 + i)
                    .build();
            companies.add(company);
        }
        supplierCompanyRepository.saveAll(companies);

        // 2. SupplierUser 생성
        List<SupplierUser> users = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            SupplierUser user = SupplierUser.builder()
                    .userId("supplierUser" + (i + 1))
                    .supplierUserName("supplierUser" + (i + 1))
                    .supplierUserEmail("supplierUser" + (i + 1) + "@supplier.com")
                    .supplierUserPhoneNumber("010-1111-222" + i)
                    .customerUserCode("CU" + String.format("%03d", i + 1))
                    .build();
            users.add(user);
        }
        supplierUserRepository.saveAll(users);

        // 3. Warehouse 생성
        List<Warehouse> warehouses = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Warehouse warehouse = Warehouse.builder()
                    .warehouseCode("WH" + String.format("%03d", i))
                    .warehouseName("창고" + i)
                    .warehouseType(i % 2 == 1 ? "ITEM" : "MATERIAL")
                    .status("ACTIVE")
                    .internalUserId("internalUser-" + i)
                    .address("경기도 천안시"+i)
                    .build();
            warehouses.add(warehouse);
        }
        warehouseRepository.saveAll(warehouses);

        // 4. Product 생성
        List<Product> products = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Product product = Product.builder()
                    .productCode("PROD" + String.format("%04d", i))
                    .productName("제품" + i)
                    .category(i % 2 == 1 ? "ITEM" : "MATERIAL")
                    .unit("EA")
                    .originPrice(BigDecimal.valueOf(10000 + i * 5000))
                    .sellingPrice(BigDecimal.valueOf(12000 + i * 6000))
                    .supplierCompany(companies.get(i - 1))
                    .build();
            products.add(product);
        }
        productRepository.saveAll(products);

        // 5. ProductStock 생성
        List<ProductStock> stocks = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            ProductStock stock = ProductStock.builder()
                    .product(products.get(i))
                    .warehouse(warehouses.get(i))
                    .totalCount(BigDecimal.valueOf(100 + i * 20))
                    .availableCount(BigDecimal.valueOf(80 + i * 15))
                    .safetyCount(BigDecimal.valueOf(50))
                    .status("NORMAL")
                    .build();
            stocks.add(stock);
        }
        productStockRepository.saveAll(stocks);

        // 6. ProductStockLog 생성
        List<ProductStockLog> logs = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            ProductStockLog log = ProductStockLog.builder()
                    .productStock(stocks.get(i))
                    .movementType(i % 2 == 0 ? "입고" : "출고")
                    .changeCount(BigDecimal.valueOf(10 + i * 5))
                    .previousCount(BigDecimal.valueOf(90 + i * 15))
                    .currentCount(BigDecimal.valueOf(100 + i * 20))
                    .fromWarehouse(i > 0 ? warehouses.get(i - 1) : null)
                    .toWarehouse(warehouses.get(i))
                    .createdById("internalUser-" + i)
                    .referenceCode("PO" + String.format("%03d", i + 1))
                    .build();
            logs.add(log);
        }
        productStockLogRepository.saveAll(logs);

        log.info("IV 도메인 목업 데이터 생성 완료");
    }

    /**
     * MM 도메인 목업 데이터 생성
     */
    private void initializeMmDomain() {
        log.info("MM 도메인 목업 데이터 생성 시작");

        // 1. ProductOrderApproval 생성
        List<ProductOrderApproval> orderApprovals = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            ProductOrderApproval approval = ProductOrderApproval.builder()
                    .approvalStatus(i % 2 == 1 ? "APPROVED" : "PENDING")
                    .approvedBy("internalUser-" + (i-1))
                    .approvedAt(LocalDateTime.now().minusDays(i))
                    .rejectedReason(i % 2 == 0 ? "검토 필요" : null)
                    .build();
            orderApprovals.add(approval);
        }
        productOrderApprovalRepository.saveAll(orderApprovals);

        // 4. ProductOrderShipment 생성
        List<ProductOrderShipment> shipments = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            ProductOrderShipment shipment = ProductOrderShipment.builder()
                    .deliveredAt(LocalDate.now().plusDays(i))
                    .expectedDelivery(LocalDate.now().plusDays(7 + i))
                    .actualDelivery(LocalDate.now().plusDays(8 + i))
                    .status(i % 2 == 1 ? "DELIVERED" : "PENDING")
                    .build();
            shipments.add(shipment);
        }
        productOrderShipmentRepository.saveAll(shipments);

        // 2. ProductOrder 생성
        List<ProductOrder> orders = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            ProductOrder order = ProductOrder.builder()
                    .productOrderCode("PO-" + String.format("%06d", 202400 + i))
                    .productOrderType(i % 2 == 1 ? "MATERIAL" : "NON_STOCK")
                    .requesterId("internalUser-" + i)
                    .approvalId(orderApprovals.get(i - 1))
                    .shipmentId(shipments.get(i - 1))
                    .totalPrice(BigDecimal.valueOf(1000000 + i * 500000))
                    .dueDate(LocalDateTime.now().plusDays(7 + i))
                    .supplierCompanyName("supplierCompany-" + i)
                    .etc("주문 비고 " + i)
                    .build();
            orders.add(order);
        }
        productOrderRepository.saveAll(orders);

        // 3. ProductOrderItem 생성
        List<ProductOrderItem> orderItems = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            ProductOrderItem item = ProductOrderItem.builder()
                    .productOrderId(orders.get(i).getId())
                    .productId("product-" + (i + 1))
                    .count(BigDecimal.valueOf(10 + i * 5))
                    .unit("EA")
                    .price(BigDecimal.valueOf(50000 + i * 10000))
                    .build();
            orderItems.add(item);
        }
        productOrderItemRepository.saveAll(orderItems);

        // 5. ProductRequestApproval 생성
        List<ProductRequestApproval> requestApprovals = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            ProductRequestApproval approval = ProductRequestApproval.builder()
                    .approvalStatus(i % 2 == 1 ? "APPROVED" : "PENDING")
                    .approvedBy(String.valueOf(4000 + i))
                    .approvedAt(LocalDateTime.now().minusDays(i))
                    .rejectedReason(i % 2 == 0 ? "추가 검토 필요" : null)
                    .build();
            requestApprovals.add(approval);
        }
        productRequestApprovalRepository.saveAll(requestApprovals);

        // 6. ProductRequest 생성
        List<ProductRequest> requests = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            ProductRequest request = ProductRequest.builder()
                    .productRequestCode("PR-" + String.format("%06d", 202400 + i))
                    .productRequestType(i % 2 == 1 ? "MATERIAL" : "NON_STOCK")
                    .requesterId(String.valueOf(5000 + i))
                    .totalPrice(BigDecimal.valueOf(500000 + i * 100000))
                    .approvalId(requestApprovals.get(i - 1))
                    .build();
            requests.add(request);
        }
        productRequestRepository.saveAll(requests);

        // 7. ProductRequestItem 생성
        List<ProductRequestItem> requestItems = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            ProductRequestItem item = ProductRequestItem.builder()
                    .productRequestId(requests.get(i).getId())
                    .productId("product-req-" + (i + 1))
                    .count(BigDecimal.valueOf(5 + i * 2))
                    .unit("EA")
                    .price(BigDecimal.valueOf(30000 + i * 5000))
                    .preferredDeliveryDate(LocalDateTime.now().plusDays(10 + i))
                    .purpose("용도 " + (i + 1))
                    .etc("요청 비고 " + (i + 1))
                    .build();
            requestItems.add(item);
        }
        productRequestItemRepository.saveAll(requestItems);

        log.info("MM 도메인 목업 데이터 생성 완료");
    }

    /**
     * PP 도메인 목업 데이터 생성
     */
    private void initializePpDomain() {
        log.info("PP 도메인 목업 데이터 생성 시작");

        // 1. Bom 생성
        List<Bom> boms = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Bom bom = Bom.builder()
                    .bomCode("BOM-" + String.format("%04d", i))
                    .description("BOM 명세서 " + i)
                    .productId("prod-" + i)
                    .version(1)
                    .leadTime(BigDecimal.valueOf(5 + i))
                    .sellingPrice(BigDecimal.valueOf(50000 + i * 10000))
                    .originPrice(BigDecimal.valueOf(40000 + i * 8000))
                    .build();
            boms.add(bom);
        }
        bomRepository.saveAll(boms);

        // 2. BomExplosion 생성
        List<BomExplosion> explosions = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            BomExplosion explosion = BomExplosion.builder()
                    .parentBomId(boms.get(i).getId())
                    .componentProductId("comp-prod-" + (i + 1))
                    .level(1)
                    .totalRequiredCount(BigDecimal.valueOf(10 + i * 5))
                    .path("/bom/" + (i + 1))
                    .routingId("routing-" + (i + 1))
                    .build();
            explosions.add(explosion);
        }
        bomExplosionRepository.saveAll(explosions);

        // 3. BomItem 생성
        List<BomItem> bomItems = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            BomItem item = BomItem.builder()
                    .bomId(boms.get(i).getId())
                    .componentType(i % 2 == 0 ? "ITEM" : "PRODUCT")
                    .componentId("comp-" + (i + 1))
                    .unit("EA")
                    .count(BigDecimal.valueOf(2 + i))
                    .build();
            bomItems.add(item);
        }
        bomItemRepository.saveAll(bomItems);

        // 4. Mps 생성
        List<Mps> mpsArr = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Mps mps = Mps.builder()
                    .mpsCode("MPS-" + String.format("%04d", i))
                    .bomId(boms.get(i - 1).getId())
                    .quotationId("quot-" + i)
                    .internalUserId("user-" + i)
                    .startWeek(LocalDate.now())
                    .endWeek(LocalDate.now().plusWeeks(4))
                    .build();
            mpsArr.add(mps);
        }
        mpsRepository.saveAll(mpsArr);

        // 5. MpsDetail 생성
        List<MpsDetail> mpsDetails = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            MpsDetail detail = MpsDetail.builder()
                    .mpsId(mpsArr.get(i).getId())
                    .weekLabel("Week " + (i + 1))
                    .demand(100 + i * 20)
                    .requiredInventory(50 + i * 10)
                    .productionNeeded(80 + i * 15)
                    .plannedProduction(90 + i * 18)
                    .build();
            mpsDetails.add(detail);
        }
        mpsDetailRepository.saveAll(mpsDetails);

        // 6. Mrp 생성
        List<Mrp> mrps = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Mrp mrp = Mrp.builder()
                    .bomId(boms.get(i - 1).getId())
                    .quotationId("quot-mrp-" + i)
                    .productId("prod-mrp-" + i)
                    .requiredCount(BigDecimal.valueOf(50 + i * 10))
                    .procurementStart(LocalDate.now().plusDays(i))
                    .expectedArrival(LocalDate.now().plusDays(7 + i))
                    .build();
            mrps.add(mrp);
        }
        mrpRepository.saveAll(mrps);

        // 7. MrpRun 생성
        List<MrpRun> mrpRuns = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            MrpRun run = MrpRun.builder()
                    .mrpResultId(mrps.get(i).getId())
                    .count(20 + i * 5)
                    .quotationId("quot-run-" + (i + 1))
                    .procurementStart(LocalDate.now().plusDays(i + 1))
                    .expectedArrival(LocalDate.now().plusDays(8 + i))
                    .status(i % 2 == 0 ? "SUCCESS" : "PENDING")
                    .build();
            mrpRuns.add(run);
        }
        mrpRunRepository.saveAll(mrpRuns);

        // 8. Operation 생성
        List<Operation> operations = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Operation operation = Operation.builder()
                    .id(UUID.randomUUID().toString())
                    .opCode("OP-" + String.format("%04d", i))
                    .opName("공정 " + i)
                    .description("공정 설명 " + i)
                    .build();
            operations.add(operation);
        }
        operationRepository.saveAll(operations);

        // 9. Routing 생성
        List<Routing> routings = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Routing routing = Routing.builder()
                    .bomItemId(bomItems.get(i).getId())
                    .operationId(operations.get(i).getId())
                    .sequence(i + 1)
                    .requiredTime(60 + i * 30) // 분 단위
                    .build();
            routings.add(routing);
        }
        routingRepository.saveAll(routings);

        log.info("PP 도메인 목업 데이터 생성 완료");
    }
}
