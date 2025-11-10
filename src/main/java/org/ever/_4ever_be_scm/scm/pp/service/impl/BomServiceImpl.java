package org.ever._4ever_be_scm.scm.pp.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.ever._4ever_be_scm.scm.iv.entity.Product;
import org.ever._4ever_be_scm.scm.iv.entity.ProductStock;
import org.ever._4ever_be_scm.scm.iv.entity.SupplierCompany;
import org.ever._4ever_be_scm.scm.iv.entity.Warehouse;
import org.ever._4ever_be_scm.scm.iv.repository.ProductRepository;
import org.ever._4ever_be_scm.scm.iv.repository.ProductStockRepository;
import org.ever._4ever_be_scm.scm.iv.repository.WarehouseRepository;
import org.ever._4ever_be_scm.scm.pp.dto.BomCreateRequestDto;
import org.ever._4ever_be_scm.scm.pp.dto.BomDetailResponseDto;
import org.ever._4ever_be_scm.scm.pp.dto.BomListResponseDto;
import org.ever._4ever_be_scm.scm.pp.dto.ProductMapResponseDto;
import org.ever._4ever_be_scm.scm.pp.dto.ProductDetailResponseDto;
import org.ever._4ever_be_scm.scm.pp.entity.*;
import org.ever._4ever_be_scm.scm.pp.repository.*;
import org.ever._4ever_be_scm.scm.pp.service.BomService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BomServiceImpl implements BomService {
    private final ProductRepository productRepository;
    private final ProductStockRepository productStockRepository;
    private final WarehouseRepository warehouseRepository;
    private final BomRepository bomRepository;
    private final BomItemRepository bomItemRepository;
    private final RoutingRepository routingRepository;
    private final BomExplosionRepository bomExplosionRepository;
    private final OperationRepository operationRepository;

    @Override
    @Transactional
    public void createBom(BomCreateRequestDto requestDto) {
        // 1. product 생성 및 저장
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String productCode = "ITM-" + uuid.substring(uuid.length() - 6);
        Product product = Product.builder()
                .productCode(productCode)
                .category("ITEM")
                .productName(requestDto.getProductName())
                .unit(requestDto.getUnit())
                .build();
        productRepository.save(product);

        // 1-1. ProductStock 생성 및 저장
        // warehouseType이 "ITEM"인 창고 조회
        Warehouse warehouse = warehouseRepository.findFirstByWarehouseType("ITEM")
                .orElseThrow(() -> new RuntimeException("ITEM 타입의 창고를 찾을 수 없습니다."));

        ProductStock productStock = ProductStock.builder()
                .product(product)
                .warehouse(warehouse)
                .status("NORMAL")
                .availableCount(BigDecimal.ZERO)
                .safetyCount(BigDecimal.ZERO)
                .reservedCount(BigDecimal.ZERO)
                .build();
        productStockRepository.save(productStock);

        // 2. BOM 생성 및 저장
        Bom bom = Bom.builder()
                .productId(product.getId())
                .bomCode("BOM-" + productCode)
                .version(1)
                .leadTime(BigDecimal.ZERO)
                .build();
        bom = bomRepository.save(bom);

        BigDecimal maxDeliveryDays = BigDecimal.ZERO;
        BigDecimal totalRequiredTime = BigDecimal.ZERO;
        BigDecimal totalOriginPrice = BigDecimal.ZERO;

        // 3. BOM_Item, Routing, BOM_Explosion 생성 및 저장
        List<BomCreateRequestDto.BomItemRequestDto> items = requestDto.getItems();
        for (BomCreateRequestDto.BomItemRequestDto item : items) {
            String productId = item.getItemId(); // 항상 productId만 입력
            Product componentProduct = productRepository.findById(productId).orElse(null);
            if (componentProduct == null) {
                throw new IllegalArgumentException("itemId " + productId + " is not found in product table");
            }
            String componentType = componentProduct.getCategory();
            String componentId = productId;
            BigDecimal originPrice = BigDecimal.ZERO;
            BigDecimal deliveryDays = BigDecimal.ZERO;

            if ("MATERIAL".equals(componentType)) {
                // 원자재: 가격, 납기 supplier에서 조회
                if (componentProduct.getOriginPrice() != null) originPrice = componentProduct.getOriginPrice();
                if (componentProduct.getSupplierCompany() != null) {
                    SupplierCompany supplier = componentProduct.getSupplierCompany();
                    if (supplier.getDeliveryDays() != null) {
                        // convert seconds -> days (fractional)
                        deliveryDays = BigDecimal.valueOf(supplier.getDeliveryDays().getSeconds())
                                .divide(BigDecimal.valueOf(86_400), 6, RoundingMode.HALF_UP);
                    }
                }
            } else if ("ITEM".equals(componentType)) {
                // 완제품(BOM): 가격, 리드타임 BOM에서 조회
                Optional<Bom>subBomOpt = bomRepository.findByProductId(productId);
                if (subBomOpt.isPresent()) {
                    Bom subBom = subBomOpt.get();
                    componentId = subBom.getId(); // 하위 BOM의 bomId
                    originPrice = subBom.getOriginPrice();
                    deliveryDays = subBom.getLeadTime();
                }
            }
            totalOriginPrice = totalOriginPrice.add(originPrice.multiply(BigDecimal.valueOf(item.getQuantity())));

            BomItem bomItem = BomItem.builder()
                    .bomId(bom.getId())
                    .componentType(componentType)
                    .componentId(componentId)
                    .unit(requestDto.getUnit())
                    .count(BigDecimal.valueOf(item.getQuantity()))
                    .build();
            bomItem = bomItemRepository.save(bomItem);

            // Operation 조회하여 requiredTime 가져오기
            Operation operation = operationRepository.findById(item.getOperationId())
                    .orElseThrow(() -> new RuntimeException("Operation을 찾을 수 없습니다: " + item.getOperationId()));
            BigDecimal operationRequiredTime = operation.getRequiredTime() != null ? operation.getRequiredTime() : BigDecimal.ZERO;

            Routing routing = Routing.builder()
                    .bomItemId(bomItem.getId())
                    .operationId(item.getOperationId())
                    .sequence(item.getSequence())
                    .requiredTime(operationRequiredTime.intValue())
                    .build();
            routing = routingRepository.save(routing);

            // 납기일/리드타임 집계
            if (deliveryDays.compareTo(maxDeliveryDays) > 0) {
                maxDeliveryDays = deliveryDays;
            }
            // totalRequiredTime =  operation.requiredTime
            totalRequiredTime = totalRequiredTime.add(operationRequiredTime);

            BomExplosion explosion = BomExplosion.builder()
                    .parentBomId(bom.getId())
                    .componentProductId(componentId)
                    .level(1)
                    .totalRequiredCount(BigDecimal.valueOf(item.getQuantity()))
                    .path(product.getProductName() + ">" + componentId)
                    .routingId(routing.getId())
                    .build();
            bomExplosionRepository.save(explosion);
        }

        // 4. lead_time 계산 및 BOM 업데이트
        // requiredTime(시간) -> 일 단위로 환산 (8시간=1일)
        BigDecimal requiredDays = totalRequiredTime.divide(BigDecimal.valueOf(480), 0, RoundingMode.UP);
        BigDecimal leadTime = requiredDays.add(maxDeliveryDays);
        bom.setLeadTime(leadTime);
        // 5. originPrice/sellingPrice 계산 (하위 품목 합산)
        bom.setOriginPrice(totalOriginPrice);
        bom.setSellingPrice(totalOriginPrice.multiply(BigDecimal.valueOf(1.3)));

        product.updatePrice(totalOriginPrice,BigDecimal.valueOf(1.3));

        productRepository.save(product);
        bomRepository.save(bom);
    }

    @Override
    @Transactional
    public void updateBom(String bomId, BomCreateRequestDto requestDto) {
        // 1. 기존 BOM 및 연관된 Product 조회
        Bom bom = bomRepository.findById(bomId)
                .orElseThrow(() -> new IllegalArgumentException("BOM not found with id: " + bomId));
        Product product = productRepository.findById(bom.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + bom.getProductId()));

        int version = bom.getVersion();
        // 2. 기존 BOMItem, Routing, BOMExplosion 삭제 (순서 중요: 외래키 제약조건 고려)
        routingRepository.deleteByBomId(bomId);
        bomExplosionRepository.deleteByParentBomId(bomId);
        bomItemRepository.deleteByBomId(bomId);

        // 3. Product 정보 업데이트
        product.setProductName(requestDto.getProductName());
        product.setUnit(requestDto.getUnit());
        productRepository.save(product);

        // 4. BOM 업데이트 시간 갱신
        bom.setVersion(version+1);

        // 5. 새로운 BOMItem, Routing, BOMExplosion 생성 (생성 로직과 동일)
        BigDecimal maxDeliveryDays = BigDecimal.ZERO;
        BigDecimal totalRequiredTime = BigDecimal.ZERO;
        BigDecimal totalOriginPrice = BigDecimal.ZERO;

        List<BomCreateRequestDto.BomItemRequestDto> items = requestDto.getItems();
        for (BomCreateRequestDto.BomItemRequestDto item : items) {
            String productId = item.getItemId(); // 항상 productId만 입력
            Product componentProduct = productRepository.findById(productId).orElse(null);
            if (componentProduct == null) {
                throw new IllegalArgumentException("itemId " + productId + " is not found in product table");
            }
            String componentType = componentProduct.getCategory();
            String componentId = productId;
            BigDecimal originPrice = BigDecimal.ZERO;
            BigDecimal deliveryDays = BigDecimal.ZERO;

            if ("MATERIAL".equals(componentType)) {
                // 원자재: 가격, 납기 supplier에서 조회
                if (componentProduct.getOriginPrice() != null) originPrice = componentProduct.getOriginPrice();
                if (componentProduct.getSupplierCompany() != null) {
                    SupplierCompany supplier = componentProduct.getSupplierCompany();
                    if (supplier.getDeliveryDays() != null) {
                        deliveryDays = BigDecimal.valueOf(supplier.getDeliveryDays().getSeconds())
                                .divide(BigDecimal.valueOf(86_400), 6, RoundingMode.HALF_UP);
                    }
                }
            } else if ("ITEM".equals(componentType)) {
                // 완제품(BOM): 가격, 리드타임 BOM에서 조회
                Optional<Bom>subBomOpt = bomRepository.findByProductId(productId);
                if (subBomOpt.isPresent()) {
                    Bom subBom = subBomOpt.get();
                    componentId = subBom.getId(); // 하위 BOM의 bomId
                    originPrice = subBom.getOriginPrice();
                    deliveryDays = subBom.getLeadTime();
                }
            }
            totalOriginPrice = totalOriginPrice.add(originPrice.multiply(BigDecimal.valueOf(item.getQuantity())));

            BomItem bomItem = BomItem.builder()
                    .bomId(bom.getId())
                    .componentType(componentType)
                    .componentId(componentId)
                    .unit(requestDto.getUnit())
                    .count(BigDecimal.valueOf(item.getQuantity()))
                    .build();
            bomItem = bomItemRepository.save(bomItem);

            // Operation 조회하여 requiredTime 가져오기
            Operation operation = operationRepository.findById(item.getOperationId())
                    .orElseThrow(() -> new RuntimeException("Operation을 찾을 수 없습니다: " + item.getOperationId()));
            BigDecimal operationRequiredTime = operation.getRequiredTime() != null ? operation.getRequiredTime() : BigDecimal.ZERO;

            Routing routing = Routing.builder()
                    .bomItemId(bomItem.getId())
                    .operationId(item.getOperationId())
                    .sequence(item.getSequence())
                    .requiredTime(operationRequiredTime.intValue())
                    .build();
            routing = routingRepository.save(routing);

            // 납기일/리드타임 집계
            if (deliveryDays.compareTo(maxDeliveryDays) > 0) {
                maxDeliveryDays = deliveryDays;
            }
            // totalRequiredTime = operation.requiredTime
            totalRequiredTime = totalRequiredTime.add(operationRequiredTime);

            BomExplosion explosion = BomExplosion.builder()
                    .parentBomId(bom.getId())
                    .componentProductId(componentId)
                    .level(1)
                    .totalRequiredCount(BigDecimal.valueOf(item.getQuantity()))
                    .path(product.getProductName() + ">" + componentId)
                    .routingId(routing.getId())
                    .build();
            bomExplosionRepository.save(explosion);
        }

        // 6. lead_time 계산 및 BOM 업데이트
        BigDecimal requiredDays = totalRequiredTime.divide(BigDecimal.valueOf(480), 0, RoundingMode.UP);
        BigDecimal leadTime = requiredDays.add(maxDeliveryDays);
        bom.setLeadTime(leadTime);
        // 7. originPrice/sellingPrice 계산 (하위 품목 합산)
        bom.setOriginPrice(totalOriginPrice);
        bom.setSellingPrice(totalOriginPrice.multiply(BigDecimal.valueOf(1.3)));

        product.updatePrice(totalOriginPrice, BigDecimal.valueOf(1.3));

        productRepository.save(product);
        bomRepository.save(bom);
    }

    @Override
    public Page<BomListResponseDto> getBomList(Pageable pageable) {
        Page<Bom> bomPage = bomRepository.findAll(pageable);
        List<BomListResponseDto> dtoList = new ArrayList<>();
        for (Bom bom : bomPage.getContent()) {
            Product product = productRepository.findById(String.valueOf(bom.getProductId())).orElse(null);
            dtoList.add(BomListResponseDto.builder()
                .bomId(String.valueOf(bom.getId()))
                .bomNumber(bom.getBomCode())
                .productId(product != null ? product.getId() : null)
                .productNumber(product != null ? product.getProductCode() : null)
                .productName(product != null ? product.getProductName() : null)
                .version("v" + bom.getVersion())
                .statusCode("ACTIVE")
                .lastModifiedAt(bom.getUpdatedAt() != null ? bom.getUpdatedAt() : null)
                .build());
        }
        return new PageImpl<>(dtoList, pageable, bomPage.getTotalElements());
    }

    @Override
    @Transactional
    public BomDetailResponseDto getBomDetail(String bomId) {
        Bom bom = bomRepository.findById(bomId).orElse(null);
        if (bom == null) return null;
        Product product = productRepository.findById(bom.getProductId()).orElse(null);
        List<BomExplosion> explosions = bomExplosionRepository.findByParentBomId(bomId);
        List<BomDetailResponseDto.BomComponentDto> components = new ArrayList<>();
        List<BomDetailResponseDto.LevelStructureDto> levelStructure = new ArrayList<>();

        //  routing 수집을 위해 BomItem을 직접 조회하고 sequence 순으로 정렬
        List<BomItem> bomItems = bomItemRepository.findByBomId(bomId);
        List<BomDetailResponseDto.RoutingDto> routingList = collectRoutingsInOrder(bomItems);

        // components와 levelStructure 구성 (기존 로직 유지)
        for (BomExplosion exp : explosions) {
            Optional<BomItem> bomItemOpt = bomItemRepository.findByBomIdAndComponentId(bomId, exp.getComponentProductId());
            if (bomItemOpt.isEmpty()) continue;
            BomItem bomItem = bomItemOpt.get();
            Optional<Routing> routingOpt = routingRepository.findByBomItemId(bomItem.getId());
            Routing routing = routingOpt.orElse(null);
            String operationName = null;
            if (routing != null && routing.getOperationId() != null) {
                operationName = operationRepository.findById(routing.getOperationId())
                    .map(Operation::getOpName).orElse(null);
            }
            Product compProduct = productRepository.findById(bomItem.getComponentId()).orElse(null);
            int parentQuantity = bomItem.getCount().intValue();
            int parentLevel = exp.getLevel();
            String levelStr = "Level " + parentLevel;
            if ("ITEM".equals(bomItem.getComponentType())) {
                BomDetailResponseDto subBomDetail = getBomDetail(bomItem.getComponentId());
                if (subBomDetail != null) {
                    //  ITEM의 실제 Product unit 조회
                    String itemUnit = bomItem.getUnit(); // 기본값
                    Optional<Bom> childBomOpt = bomRepository.findById(bomItem.getComponentId());
                    if (childBomOpt.isPresent()) {
                        Optional<Product> childProductOpt = productRepository.findById(childBomOpt.get().getProductId());
                        if (childProductOpt.isPresent()) {
                            itemUnit = childProductOpt.get().getUnit();
                        }
                    }

                    components.add(BomDetailResponseDto.BomComponentDto.builder()
                        .itemId(subBomDetail.getBomId())
                        .code(subBomDetail.getProductNumber())
                        .name(subBomDetail.getProductName())
                        .quantity(parentQuantity)
                        .unit(itemUnit)  //  해당 제품의 실제 unit
                        .level(levelStr)
                        .supplierName(null)
                        .componentType(bomItem.getComponentType())
                        .build());
                    // 하위 BOM의 구성품을 parentQuantity만큼 곱해서, 레벨+1로 추가
                    for (BomDetailResponseDto.BomComponentDto subComp : subBomDetail.getComponents()) {
                        components.add(BomDetailResponseDto.BomComponentDto.builder()
                            .itemId(subComp.getItemId())
                            .code(subComp.getCode())
                            .name(subComp.getName())
                            .quantity(subComp.getQuantity() * parentQuantity)
                            .unit(subComp.getUnit())  //  이미 올바른 unit
                            .level("Level " + (parentLevel + 1))
                            .supplierName(subComp.getSupplierName())
                            .componentType(subComp.getComponentType())
                            .build());
                    }
                }
            } else if (compProduct != null) {
                //  MATERIAL의 실제 Product unit 사용
                components.add(BomDetailResponseDto.BomComponentDto.builder()
                    .itemId(compProduct.getId())
                    .code(compProduct.getProductCode())
                    .name(compProduct.getProductName())
                    .quantity(parentQuantity)
                    .unit(compProduct.getUnit())  //  해당 제품의 실제 unit
                    .level(levelStr)
                    .supplierName(compProduct.getSupplierCompany() != null ? compProduct.getSupplierCompany().getCompanyName() : null)
                    .componentType(bomItem.getComponentType())
                    .build());
            }
        }

        // levelStructure 생성 (트리 구조)
        if (product != null) {
            // 1. 최상위 BOM을 root로 추가
            levelStructure.add(BomDetailResponseDto.LevelStructureDto.builder()
                    .id("root-" + bomId)
                    .code(bom.getBomCode())
                    .name(product.getProductName())
                    .level(0)
                    .parentId(null)
                    .build());

            // 2. 재귀적으로 하위 구조 생성
            buildLevelStructure(bomId, "root-" + bomId, 1, levelStructure);
        }

        //  routing은 이미 올바른 순서로 수집되었으므로 1부터 재부여만
        int seqNum = 1;
        for (BomDetailResponseDto.RoutingDto r : routingList) {
            r.setSequence(seqNum++);
        }

        //  components에서 itemId가 같은 항목들을 합산 (중복 제거)
        List<BomDetailResponseDto.BomComponentDto> mergedComponents = mergeComponentsByItemId(components);

        return BomDetailResponseDto.builder()
            .bomId(bom.getId())
            .bomNumber(bom.getBomCode())
            .productId(product != null ? product.getId() : null)
            .productNumber(product != null ? product.getProductCode() : null)
            .productName(product != null ? product.getProductName() : null)
            .version("v" + bom.getVersion())
            .statusCode("ACTIVE")
            .lastModifiedAt(bom.getUpdatedAt() != null ? bom.getUpdatedAt() : null)
            .components(mergedComponents)
            .levelStructure(levelStructure)
            .routing(routingList)
            .build();
    }

    /**
     * BomItem을 routing sequence 순으로 처리하여 올바른 순서로 routing 수집
     * ITEM의 경우 하위 BOM routing을 먼저 추가한 후, ITEM 자체 routing을 추가
     */
    private List<BomDetailResponseDto.RoutingDto> collectRoutingsInOrder(List<BomItem> bomItems) {
        List<BomDetailResponseDto.RoutingDto> result = new ArrayList<>();

        // 1. BomItem을 routing sequence로 정렬하기 위해 <BomItem, Routing> 쌍으로 수집
        List<BomItemWithRouting> itemsWithRouting = new ArrayList<>();
        for (BomItem bomItem : bomItems) {
            Optional<Routing> routingOpt = routingRepository.findByBomItemId(bomItem.getId());
            if (routingOpt.isPresent()) {
                itemsWithRouting.add(new BomItemWithRouting(bomItem, routingOpt.get()));
            }
        }

        // 2. routing sequence 순으로 정렬
        itemsWithRouting.sort(Comparator.comparingInt(item -> item.routing.getSequence()));

        // 3. 정렬된 순서대로 처리
        for (BomItemWithRouting item : itemsWithRouting) {
            BomItem bomItem = item.bomItem;
            Routing routing = item.routing;

            if ("ITEM".equals(bomItem.getComponentType())) {
                //  ITEM의 경우: 하위 BOM의 routing을 먼저 수집
                String childBomId = bomItem.getComponentId(); // ITEM의 componentId는 BOM ID
                List<BomItem> childBomItems = bomItemRepository.findByBomId(childBomId);
                List<BomDetailResponseDto.RoutingDto> childRoutings = collectRoutingsInOrder(childBomItems);
                result.addAll(childRoutings);

                //  그 다음에 ITEM 자체의 routing 추가 (조립 공정)
                String operationName = null;
                if (routing.getOperationId() != null) {
                    operationName = operationRepository.findById(routing.getOperationId())
                        .map(Operation::getOpName).orElse(null);
                }

                // ITEM의 Product 조회 (BOM → productId → Product)
                String productName = null;
                Optional<Bom> bomOpt = bomRepository.findById(childBomId);
                if (bomOpt.isPresent()) {
                    String productId = bomOpt.get().getProductId();
                    productName = productRepository.findById(productId)
                        .map(Product::getProductName).orElse(null);
                }

                result.add(BomDetailResponseDto.RoutingDto.builder()
                    .sequence(0)  // 임시 값, 나중에 재부여됨
                    .itemName(productName)
                    .operationName(operationName)
                    .runTime(routing.getRequiredTime())
                    .build());

            } else {
                //  MATERIAL의 경우: routing 그대로 추가
                String operationName = null;
                if (routing.getOperationId() != null) {
                    operationName = operationRepository.findById(routing.getOperationId())
                        .map(Operation::getOpName).orElse(null);
                }

                // MATERIAL의 Product 조회 (componentId가 바로 productId)
                String productName = productRepository.findById(bomItem.getComponentId())
                    .map(Product::getProductName).orElse(null);

                result.add(BomDetailResponseDto.RoutingDto.builder()
                    .sequence(0)  // 임시 값, 나중에 재부여됨
                    .itemName(productName)
                    .operationName(operationName)
                    .runTime(routing.getRequiredTime())
                    .build());
            }
        }

        return result;
    }

    /**
     * BomItem과 Routing을 함께 저장하는 헬퍼 클래스
     */
    private static class BomItemWithRouting {
        BomItem bomItem;
        Routing routing;

        BomItemWithRouting(BomItem bomItem, Routing routing) {
            this.bomItem = bomItem;
            this.routing = routing;
        }
    }

    /**
     * levelStructure를 재귀적으로 생성 (트리 구조)
     *
     * @param bomId 현재 BOM ID
     * @param parentNodeId 부모 노드의 ID
     * @param currentLevel 현재 레벨
     * @param levelStructure 결과를 담을 리스트
     */
    private void buildLevelStructure(String bomId, String parentNodeId, int currentLevel,
                                     List<BomDetailResponseDto.LevelStructureDto> levelStructure) {
        // 현재 BOM의 모든 항목 조회
        List<BomItem> bomItems = bomItemRepository.findByBomId(bomId);

        for (BomItem bomItem : bomItems) {
            String componentType = bomItem.getComponentType();
            String componentId = bomItem.getComponentId();
            Integer quantity = bomItem.getCount().intValue();
            String unit = bomItem.getUnit();

            if ("ITEM".equals(componentType)) {
                // ITEM인 경우: 하위 BOM을 노드로 추가
                Optional<Bom> childBomOpt = bomRepository.findById(componentId);
                if (childBomOpt.isPresent()) {
                    Bom childBom = childBomOpt.get();
                    Optional<Product> childProductOpt = productRepository.findById(childBom.getProductId());

                    if (childProductOpt.isPresent()) {
                        Product childProduct = childProductOpt.get();
                        String nodeId = "bom-" + componentId;

                        // 하위 BOM을 노드로 추가
                        levelStructure.add(BomDetailResponseDto.LevelStructureDto.builder()
                                .id(nodeId)
                                .code(childBom.getBomCode())
                                .name(childProduct.getProductName())
                                .quantity(quantity)
                                .unit(childProduct.getUnit())
                                .level(currentLevel)
                                .parentId(parentNodeId)
                                .build());

                        // 재귀 호출: 하위 BOM의 구성품들 추가
                        buildLevelStructure(componentId, nodeId, currentLevel + 1, levelStructure);
                    }
                }
            } else if ("MATERIAL".equals(componentType)) {
                // MATERIAL인 경우: 원자재를 노드로 추가
                Optional<Product> productOpt = productRepository.findById(componentId);
                if (productOpt.isPresent()) {
                    Product product = productOpt.get();
                    String nodeId = "material-" + componentId;

                    levelStructure.add(BomDetailResponseDto.LevelStructureDto.builder()
                            .id(nodeId)
                            .code(product.getProductCode())
                            .name(product.getProductName())
                            .quantity(quantity)
                            .unit(product.getUnit())
                            .level(currentLevel)
                            .parentId(parentNodeId)
                            .build());
                }
            }
        }
    }

    /**
     * itemId가 같은 component들을 합산 (수량만 합산, 나머지는 첫 번째 항목 유지)
     */
    private List<BomDetailResponseDto.BomComponentDto> mergeComponentsByItemId(
            List<BomDetailResponseDto.BomComponentDto> components) {

        Map<String, BomDetailResponseDto.BomComponentDto> mergedMap = new LinkedHashMap<>();

        for (BomDetailResponseDto.BomComponentDto component : components) {
            String itemId = component.getItemId();

            if (mergedMap.containsKey(itemId)) {
                // 이미 존재하면 수량만 합산
                BomDetailResponseDto.BomComponentDto existing = mergedMap.get(itemId);
                existing.setQuantity(existing.getQuantity() + component.getQuantity());
            } else {
                // 새로운 항목이면 추가 (operationId, operationName 제거)
                mergedMap.put(itemId, BomDetailResponseDto.BomComponentDto.builder()
                    .itemId(component.getItemId())
                    .code(component.getCode())
                    .name(component.getName())
                    .quantity(component.getQuantity())
                    .unit(component.getUnit())
                    .level(component.getLevel())
                    .supplierName(component.getSupplierName())
                    .componentType(component.getComponentType())
                    .build());
            }
        }

        return new ArrayList<>(mergedMap.values());
    }

    @Override
    public List<ProductMapResponseDto> getProductMap() {
        // ProductStock에 존재하는 Product만 조회 (DISTINCT로 중복 제거)
        List<Product> products = productStockRepository.findAllProductsInStock();

        return products.stream()
                .map(product -> ProductMapResponseDto.builder()
                        .key(product.getId())
                        .value(product.getProductName())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public ProductDetailResponseDto getProductDetail(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product를 찾을 수 없습니다: " + productId));

        String supplierName = null;
        if (product.getSupplierCompany() != null) {
            supplierName = product.getSupplierCompany().getCompanyName();
        }

        return ProductDetailResponseDto.builder()
                .productId(product.getId())
                .productName(product.getProductName())
                .category(product.getCategory())
                .productNumber(product.getProductCode())
                .uomName(product.getUnit())
                .unitPrice(product.getOriginPrice())
                .supplierName(supplierName)
                .build();
    }

    @Override
    public List<ProductMapResponseDto> getOperationMap() {
        List<Operation> operations = operationRepository.findAll();

        return operations.stream()
                .map(operation -> ProductMapResponseDto.builder()
                        .key(operation.getId())
                        .value(operation.getOpName())
                        .build())
                .toList();
    }
}
