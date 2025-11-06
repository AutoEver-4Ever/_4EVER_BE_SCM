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
                log.info("목업 기본 데이터가 이미 존재합니다. 섹션별 후처리만 수행합니다.");
                // 공급사 자재 및 BOM-자재 연동 후처리만 수행
                initializeSupplierMaterials();
                linkBOMMaterialsToSupplierMaterials();
                return;
            }

            // 1. IV 도메인 데이터 생성
            initializeIvDomain();
            
            // 2. MM 도메인 데이터 생성
            initializeMmDomain();
            
            // 3. PP 도메인 데이터 생성 + 외장재 BOM 생성 (이미 내부에서 호출됨)
            initializePpDomain();

            // 4. 공급사 MATERIAL 적재 및 BOM MATERIAL 연동
            initializeSupplierMaterials();
            linkBOMMaterialsToSupplierMaterials();
            
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

        // 1. SupplierUser 생성
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

        // 2. SupplierCompany 생성
        List<SupplierCompany> companies = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            SupplierCompany company = SupplierCompany.builder()
                    .supplierUser(users.get(i-1))
                    .companyCode("SUP" + String.format("%03d", i))
                    .companyName("공급업체" + i)
                    .category(i % 2 == 1 ? "ITEM" : "MATERIAL")
                    .baseAddress("서울시 강남구")
                    .detailAddress("테헤란로 " + (100 + i) + "길")
                    .status("ACTIVE")
                    .officePhone("02-1234-567" + i)
                    .deliveryDays(java.time.Duration.ofSeconds((3 + i) * 86_400L))
                    .build();
            companies.add(company);
        }
        supplierCompanyRepository.saveAll(companies);

        // 3. Warehouse 생성
        List<Warehouse> warehouses = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            String createdById = null;

            // internel1 ~ internel3까지만 설정
            if (i > 3) {
                createdById = "internel" + 2;
            }
            else
                createdById = "internel" + i;

            Warehouse warehouse = Warehouse.builder()
                    .warehouseCode("WH" + String.format("%03d", i))
                    .warehouseName("창고" + i)
                    .warehouseType(i % 2 == 1 ? "ITEM" : "MATERIAL")
                    .status("ACTIVE")
                    .internalUserId(createdById)
                    .location("경기도 천안시"+i)
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
                    .category("MATERIAL")
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
            String createdById = null;

            // internel1 ~ internel3까지만 설정
            if (i==0 || i > 3) {
                createdById = "internel" + 2;
            }
            else
                createdById = "internel" + i;

            ProductStockLog log = ProductStockLog.builder()
                    .productStock(stocks.get(i))
                    .movementType(i % 2 == 0 ? "입고" : "출고")
                    .changeCount(BigDecimal.valueOf(10 + i * 5))
                    .previousCount(BigDecimal.valueOf(90 + i * 15))
                    .currentCount(BigDecimal.valueOf(100 + i * 20))
                    .fromWarehouse(i > 0 ? warehouses.get(i - 1) : null)
                    .toWarehouse(warehouses.get(i))
                    .createdById(createdById)
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
                    .approvalStatus(i % 2 == 1 ? "APPROVAL" : "PENDING")
                    .approvedBy("internel" + (i-1))
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
                    .requesterId("internel" + i)
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
                    .approvalStatus(i % 2 == 1 ? "APPROVAL" : "PENDING")
                    .approvedBy("internel" + (i-1))
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
                    .requesterId("internel" + i)
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
                    .componentType(i % 2 == 0 ? "ITEM" : "MATERIAL")
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
                    .productId(mrps.get(i).getProductId())
                    .quantity(BigDecimal.valueOf(20 + i * 5))
                    .quotationId("quot-run-" + (i + 1))
                    .procurementStart(LocalDate.now().plusDays(i + 1))
                    .expectedArrival(LocalDate.now().plusDays(8 + i))
                    .status(i % 2 == 0 ? "APPROVAL" : "PENDING")
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
                    .requiredTime(BigDecimal.valueOf(110+(i*10)))
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

        // 추가: 자동차 외장재 30건 BOM(1/2/3-Depth) 생성
        try {
            initializeExteriorBoms();
        } catch (Exception e) {
            log.warn("외장재 BOM 생성 중 오류 발생: {}", e.getMessage());
        }
    }

    /**
     * 공급사별 MATERIAL 카탈로그 생성 및 재고 초기화
     */
    private void initializeSupplierMaterials() {
        List<SupplierCompany> suppliers = supplierCompanyRepository.findAll();
        if (suppliers.isEmpty()) {
            log.info("공급사가 없어 MATERIAL 적재를 스킵합니다.");
            return;
        }

        Warehouse materialWarehouse = warehouseRepository.findAll().stream()
                .filter(w -> "MATERIAL".equalsIgnoreCase(w.getWarehouseType()))
                .findFirst()
                .orElseGet(() -> warehouseRepository.save(Warehouse.builder()
                        .warehouseCode("WH-MAT")
                        .warehouseName("Materials")
                        .warehouseType("MATERIAL")
                        .status("ACTIVE")
                        .location("Main DC")
                        .build()));

        record MatDef(String family, String spec, String name, String uom, BigDecimal price) {}

        List<MatDef> catalog = List.of(
                new MatDef("AL","PLATE-2T","알루미늄 판재 2T","EA", new BigDecimal("12000")),
                new MatDef("AL","PROFILE-20X20","알루미늄 프로파일 20x20","M", new BigDecimal("5000")),
                new MatDef("ST","REINF-BAR","스틸 리인포스 바","EA", new BigDecimal("9000")),
                new MatDef("SS","BOLT-M6-20","스테인리스 볼트 M6x20","EA", new BigDecimal("150")),
                new MatDef("SS","NUT-M6","스테인리스 너트 M6","EA", new BigDecimal("80")),
                new MatDef("SS","WASHER-M6","스테인리스 와셔 M6","EA", new BigDecimal("50")),
                new MatDef("PF","CLIP-STD","표준 고정 클립","EA", new BigDecimal("200")),
                new MatDef("PT","PRIMER-BLK","프라이머 블랙","KG", new BigDecimal("78000")),
                new MatDef("PT","BASE-BLK","베이스코트 블랙","KG", new BigDecimal("82000")),
                new MatDef("PT","CLEAR","클리어코트","KG", new BigDecimal("90000")),
                new MatDef("AD","3M-ADHESIVE","3M 접착제","EA", new BigDecimal("5000")),
                new MatDef("TP","AFT-15-1.0","아크릴폼테이프 15mm/1.0mm","M", new BigDecimal("900"))
        );

        int sIdx = 0;
        for (MatDef def : catalog) {
            SupplierCompany sup = suppliers.get(sIdx % suppliers.size());
            sIdx++;
            String code = "MAT-" + safeCode(sup.getCompanyCode()) + "-" + def.family + "-" + def.spec;

            Product p = productRepository.findById(code).orElseGet(() -> {
                Product np = Product.builder()
                        .id(code)
                        .productCode(code)
                        .category("MATERIAL")
                        .supplierCompany(sup)
                        .productName(def.name)
                        .unit(def.uom)
                        .originPrice(def.price)
                        .sellingPrice(def.price.multiply(new BigDecimal("1.2")))
                        .build();
                return productRepository.save(np);
            });

            // 재고 upsert
            ProductStock stock = productStockRepository.findAll().stream()
                    .filter(ps -> ps.getProduct() != null && ps.getProduct().getId().equals(p.getId()))
                    .findFirst()
                    .orElseGet(() -> ProductStock.builder()
                            .product(p)
                            .warehouse(materialWarehouse)
                            .status("NORMAL")
                            .availableCount(BigDecimal.ZERO)
                            .safetyCount(new BigDecimal("30"))
                            .reservedCount(BigDecimal.ZERO)
                            .build());
            if (stock.getId() == null) {
                stock.setAvailableCount(new BigDecimal(100));
                productStockRepository.save(stock);
            }
        }

        log.info("공급사 MATERIAL {}건 적재 완료 (공급사 수: {})", catalog.size(), suppliers.size());
    }

    /**
     * 기존 BOM의 MATERIAL을 공급사 MATERIAL로 치환
     */
    private void linkBOMMaterialsToSupplierMaterials() {
        List<SupplierCompany> suppliers = supplierCompanyRepository.findAll();
        if (suppliers.isEmpty()) return;
        SupplierCompany sup = suppliers.get(0); // 기준 공급사 한 곳으로 매핑

        // 매핑 테이블: 기존 generic id -> 공급사 MATERIAL 코드
        java.util.Map<String, String> map = new java.util.HashMap<>();
        map.put("PROD-AL-PLATE-2T", "MAT-" + safeCode(sup.getCompanyCode()) + "-AL-PLATE-2T");
        map.put("PROD-PAINT-BLK", "MAT-" + safeCode(sup.getCompanyCode()) + "-PT-BASE-BLK");
        map.put("PROD-CLIP-STD", "MAT-" + safeCode(sup.getCompanyCode()) + "-PF-CLIP-STD");
        map.put("PROD-STEEL-FRAME-DOOR", "MAT-" + safeCode(sup.getCompanyCode()) + "-ST-REINF-BAR");
        map.put("PROD-PAINT-CLEAR", "MAT-" + safeCode(sup.getCompanyCode()) + "-PT-CLEAR");
        map.put("PROD-ADHESIVE-3M", "MAT-" + safeCode(sup.getCompanyCode()) + "-AD-3M-ADHESIVE");

        List<BomItem> items = bomItemRepository.findAll();
        int changed = 0;
        for (BomItem it : items) {
            if (!"MATERIAL".equalsIgnoreCase(it.getComponentType())) continue;
            String target = map.get(it.getComponentId());
            if (target == null) continue;
            if (productRepository.findById(target).isEmpty()) continue;
            it.setComponentId(target);
            bomItemRepository.save(it);
            changed++;
        }

        log.info("BOM MATERIAL 치환 완료: {}건", changed);
    }

    private String safeCode(String s) {
        if (s == null) return "SUP";
        return s.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }

    /**
     * 자동차 외장재 30건 BOM 시드
     * - 1-Depth: 15건 (MATERIAL만, 내부 공정 Routing 포함)
     * - 2-Depth: 10건 (1-Depth 제품을 ITEM으로 참조, 조립 Routing 포함)
     * - 3-Depth: 5건  (2-Depth 제품을 ITEM으로 참조, 최종 조립 Routing 포함)
     * Routing.required_time 단위: 초(5~600)
     */
    private void initializeExteriorBoms() {
        // 멱등 가드: 이미 30건 이상 BOM이 있으면 스킵
        if (bomRepository.count() >= 30) {
            log.info("BOM이 이미 30건 이상 존재합니다. 외장재 BOM 시드를 스킵합니다.");
            return;
        }

        log.info("외장재 BOM 30건 생성 시작");

        // 0) 재사용할 MATERIAL Product 생성 (없으면 간단 생성)
        Product alPlate = ensureMaterialProduct("PROD-AL-PLATE-2T", "알루미늄 판재 2T", "EA", new java.math.BigDecimal("12000"));
        Product paintBlack = ensureMaterialProduct("PROD-PAINT-BLK", "도료 블랙", "KG", new java.math.BigDecimal("80000"));
        Product clipStd = ensureMaterialProduct("PROD-CLIP-STD", "고정 클립 표준", "EA", new java.math.BigDecimal("200"));
        Product steelFrameDoor = ensureMaterialProduct("PROD-STEEL-FRAME-DOOR", "강철 프레임(도어)", "EA", new java.math.BigDecimal("35000"));
        Product paintClear = ensureMaterialProduct("PROD-PAINT-CLEAR", "클리어 코트", "KG", new java.math.BigDecimal("90000"));
        Product adhesive3m = ensureMaterialProduct("PROD-ADHESIVE-3M", "3M 접착제", "EA", new java.math.BigDecimal("5000"));

        // 1) Operation 풀 구성(필요 최소)
        java.util.List<Operation> ops = new java.util.ArrayList<>();
        ops.add(createOperation("OP-ASSEMBLY", "조립", 180));
        ops.add(createOperation("OP-ULTRA-WELD", "초음파용접", 90));
        ops.add(createOperation("OP-TAPE-APPLY", "테이프 부착", 60));
        ops.add(createOperation("OP-INSPECTION", "검사", 90));
        ops.add(createOperation("OP-PACKING", "포장", 60));
        ops.add(createOperation("OP-SURFACE-PREP", "표면처리", 60));
        ops.add(createOperation("OP-FORMING", "성형", 180));
        ops.add(createOperation("OP-PRIMER", "프라이머", 120));
        ops.add(createOperation("OP-BASECOAT", "베이스코트", 240));
        ops.add(createOperation("OP-CLEARCOAT", "클리어코트", 180));
        ops.add(createOperation("OP-CURING", "건조", 300));
        operationRepository.saveAll(ops);

        // 2) 1-Depth BOM 15건 (리프, MATERIAL만 + 내부 공정 Routing)
        java.util.List<Bom> depth1Boms = new java.util.ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            String prodId = String.format("PROD-EXT-%03d", i);
            Product prod = ensureItemProduct(prodId, "외장 부품 " + i, "EA");

            Bom bom = Bom.builder()
                    .productId(prod.getId())
                    .description("1-Depth Leaf BOM - 외장 부품 " + i)
                    .version(1)
                    .leadTime(java.math.BigDecimal.ZERO)
                    .originPrice(new java.math.BigDecimal("0"))
                    .sellingPrice(new java.math.BigDecimal("0"))
                    .build();
            bom = bomRepository.save(bom);
            bom.setBomCode("BOM-" + trailing7(bom.getId()));
            bomRepository.save(bom);

            // MATERIAL 2~3개 구성
            BomItem m1 = BomItem.builder().bomId(bom.getId()).componentType("MATERIAL").componentId(alPlate.getId()).unit("EA").count(new java.math.BigDecimal("1")).build();
            BomItem m2 = BomItem.builder().bomId(bom.getId()).componentType("MATERIAL").componentId(paintBlack.getId()).unit("KG").count(new java.math.BigDecimal("0.15")).build();
            BomItem m3 = BomItem.builder().bomId(bom.getId()).componentType("MATERIAL").componentId(clipStd.getId()).unit("EA").count(new java.math.BigDecimal("8")).build();
            m1 = bomItemRepository.save(m1);
            m2 = bomItemRepository.save(m2);
            m3 = bomItemRepository.save(m3);

            // 내부 공정 Routing: 대표 MATERIAL(m1)에 연결 (5~600초 범위)
            createRoutingChainSeconds(m1.getId(), new int[]{secs(60+i), secs(100+i), secs(140+i), secs(180+i), secs(220+i), secs(260+i)},
                    new String[]{"OP-SURFACE-PREP","OP-FORMING","OP-PRIMER","OP-BASECOAT","OP-CLEARCOAT","OP-CURING"}, ops);
            createRoutingChainSeconds(m1.getId(), new int[]{secs(40+i), secs(30+i)}, new String[]{"OP-INSPECTION","OP-PACKING"}, ops, 7);

            // Explosion 기록(level=1)
            saveExplosion(bom.getId(), alPlate.getId(), 1, m1.getId());
            saveExplosion(bom.getId(), paintBlack.getId(), 1, null);
            saveExplosion(bom.getId(), clipStd.getId(), 1, null);

            depth1Boms.add(bom);
        }

        // 3) 2-Depth BOM 10건 (1-Depth 참조 + 조립 Routing)
        java.util.List<Bom> depth2Boms = new java.util.ArrayList<>();
        for (int i = 16; i <= 25; i++) {
            String prodId = String.format("PROD-EXT-%03d", i);
            Product prod = ensureItemProduct(prodId, "외장 서브어셈블리 " + i, "EA");
            Bom bom = Bom.builder()
                    .productId(prod.getId())
                    .description("2-Depth Sub-Assembly BOM - 외장 서브어셈블리 " + i)
                    .version(1)
                    .leadTime(java.math.BigDecimal.ZERO)
                    .originPrice(new java.math.BigDecimal("0"))
                    .sellingPrice(new java.math.BigDecimal("0"))
                    .build();
            bom = bomRepository.save(bom);
            bom.setBomCode("BOM-" + trailing7(bom.getId()));
            bomRepository.save(bom);

            // 하위 1-Depth 참조 2개 + MATERIAL 1~2개
            Bom ref1 = depth1Boms.get((i - 16) % depth1Boms.size());
            Bom ref2 = depth1Boms.get((i - 10) % depth1Boms.size());
            BomItem i1 = BomItem.builder().bomId(bom.getId()).componentType("ITEM").componentId(ref1.getProductId()).unit("EA").count(new java.math.BigDecimal("1")).build();
            BomItem i2 = BomItem.builder().bomId(bom.getId()).componentType("ITEM").componentId(ref2.getProductId()).unit("EA").count(new java.math.BigDecimal("1")).build();
            BomItem m1 = BomItem.builder().bomId(bom.getId()).componentType("MATERIAL").componentId(steelFrameDoor.getId()).unit("EA").count(new java.math.BigDecimal("1")).build();
            BomItem m2 = BomItem.builder().bomId(bom.getId()).componentType("MATERIAL").componentId(paintClear.getId()).unit("KG").count(new java.math.BigDecimal("0.05")).build();
            i1 = bomItemRepository.save(i1);
            i2 = bomItemRepository.save(i2);
            m1 = bomItemRepository.save(m1);
            m2 = bomItemRepository.save(m2);

            // 조립 Routing: 대표 i1에 연결
            createRoutingChainSeconds(i1.getId(), new int[]{secs(180+i), secs(90+i), secs(60+i), secs(45+i)},
                    new String[]{"OP-ASSEMBLY","OP-ULTRA-WELD","OP-INSPECTION","OP-PACKING"}, ops);

            // Explosion(level=2)
            saveExplosion(bom.getId(), ref1.getProductId(), 2, null);
            saveExplosion(bom.getId(), ref2.getProductId(), 2, null);
            saveExplosion(bom.getId(), steelFrameDoor.getId(), 2, null);
            saveExplosion(bom.getId(), paintClear.getId(), 2, null);

            depth2Boms.add(bom);
        }

        // 4) 3-Depth BOM 5건 (2-Depth 참조 + 최종 조립 Routing)
        for (int i = 26; i <= 30; i++) {
            String prodId = String.format("PROD-EXT-%03d", i);
            Product prod = ensureItemProduct(prodId, "외장 패키지 " + i, "EA");
            Bom bom = Bom.builder()
                    .productId(prod.getId())
                    .description("3-Depth Package BOM - 외장 패키지 " + i)
                    .version(1)
                    .leadTime(java.math.BigDecimal.ZERO)
                    .originPrice(new java.math.BigDecimal("0"))
                    .sellingPrice(new java.math.BigDecimal("0"))
                    .build();
            bom = bomRepository.save(bom);
            bom.setBomCode("BOM-" + trailing7(bom.getId()));
            bomRepository.save(bom);

            Bom refA = depth2Boms.get((i - 26) % depth2Boms.size());
            Bom refB = depth2Boms.get((i - 23) % depth2Boms.size());
            BomItem i1 = BomItem.builder().bomId(bom.getId()).componentType("ITEM").componentId(refA.getProductId()).unit("EA").count(new java.math.BigDecimal("1")).build();
            BomItem i2 = BomItem.builder().bomId(bom.getId()).componentType("ITEM").componentId(refB.getProductId()).unit("EA").count(new java.math.BigDecimal("1")).build();
            BomItem m1 = BomItem.builder().bomId(bom.getId()).componentType("MATERIAL").componentId(adhesive3m.getId()).unit("EA").count(new java.math.BigDecimal("2")).build();
            i1 = bomItemRepository.save(i1);
            i2 = bomItemRepository.save(i2);
            m1 = bomItemRepository.save(m1);

            // 최종 조립 Routing: 대표 i1
            createRoutingChainSeconds(i1.getId(), new int[]{secs(240+i), secs(60+i), secs(90+i), secs(60+i)},
                    new String[]{"OP-ASSEMBLY","OP-TAPE-APPLY","OP-INSPECTION","OP-PACKING"}, ops);

            // Explosion(level=3)
            saveExplosion(bom.getId(), refA.getProductId(), 3, null);
            saveExplosion(bom.getId(), refB.getProductId(), 3, null);
            saveExplosion(bom.getId(), adhesive3m.getId(), 3, null);
        }

        log.info("외장재 BOM 30건 생성 완료");
    }

    private Product ensureMaterialProduct(String id, String name, String unit, java.math.BigDecimal originPrice) {
        return productRepository.findById(id).orElseGet(() -> {
            Product p = Product.builder()
                    .id(id)
                    .productCode(id)
                    .category("MATERIAL")
                    .productName(name)
                    .unit(unit)
                    .originPrice(originPrice)
                    .sellingPrice(originPrice.multiply(new java.math.BigDecimal("1.2")))
                    .build();
            return productRepository.save(p);
        });
    }

    private Product ensureItemProduct(String id, String name, String unit) {
        return productRepository.findById(id).orElseGet(() -> {
            Product p = Product.builder()
                    .id(id)
                    .productCode(id)
                    .category("ITEM")
                    .productName(name)
                    .unit(unit)
                    .originPrice(new java.math.BigDecimal("0"))
                    .sellingPrice(new java.math.BigDecimal("0"))
                    .build();
            return productRepository.save(p);
        });
    }

    private Operation createOperation(String code, String name, int defaultMinutes) {
        return Operation.builder()
                .id(java.util.UUID.randomUUID().toString())
                .opCode(code)
                .opName(name)
                .requiredTime(java.math.BigDecimal.valueOf(defaultMinutes))
                .description(name + " 표준 공정")
                .build();
    }

    private void createRoutingChainSeconds(String bomItemId, int[] seconds, String[] opCodes, java.util.List<Operation> ops) {
        createRoutingChainSeconds(bomItemId, seconds, opCodes, ops, 1);
    }

    private void createRoutingChainSeconds(String bomItemId, int[] seconds, String[] opCodes, java.util.List<Operation> ops, int startSeq) {
        int seq = startSeq;
        for (int i = 0; i < seconds.length && i < opCodes.length; i++) {
            String opCode = opCodes[i];
            String opId = ops.stream().filter(o -> opCode.equals(o.getOpCode())).map(Operation::getId).findFirst().orElse(ops.get(0).getId());
            Routing r = Routing.builder()
                    .bomItemId(bomItemId)
                    .operationId(opId)
                    .sequence(seq++)
                    .requiredTime(Math.max(5, Math.min(600, seconds[i])))
                    .build();
            routingRepository.save(r);
        }
    }

    private void saveExplosion(String parentBomId, String componentProductId, int level, String routingId) {
        BomExplosion exp = BomExplosion.builder()
                .parentBomId(parentBomId)
                .componentProductId(componentProductId)
                .level(level)
                .totalRequiredCount(java.math.BigDecimal.ONE)
                .path("/bom/" + parentBomId)
                .routingId(routingId)
                .build();
        bomExplosionRepository.save(exp);
    }

    private int secs(int base) {
        // 5~600 범위로 fold
        int v = (base * 37) % 600;
        return Math.max(5, v);
    }

    private String trailing7(String s) {
        if (s == null) return "0000000";
        String compact = s.replaceAll("[^A-Za-z0-9]", "");
        int len = compact.length();
        return (len <= 7) ? compact : compact.substring(len - 7);
    }
}
