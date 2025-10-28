package org.ever._4ever_be_scm.scm.iv.service.impl;

import lombok.RequiredArgsConstructor;
import org.ever._4ever_be_scm.common.exception.BusinessException;
import org.ever._4ever_be_scm.scm.iv.dto.InventoryItemDetailDto;
import org.ever._4ever_be_scm.scm.iv.dto.InventoryItemDto;
import org.ever._4ever_be_scm.scm.iv.dto.PagedResponseDto;
import org.ever._4ever_be_scm.scm.iv.dto.PageResponseDto;
import org.ever._4ever_be_scm.scm.iv.dto.ShortageItemDto;
import org.ever._4ever_be_scm.scm.iv.dto.ShortageItemPreviewDto;
import org.ever._4ever_be_scm.scm.iv.dto.StockMovementDto;
import org.ever._4ever_be_scm.scm.iv.dto.request.AddInventoryItemRequest;
import org.ever._4ever_be_scm.scm.iv.entity.*;
import org.ever._4ever_be_scm.scm.iv.repository.ProductStockLogRepository;
import org.ever._4ever_be_scm.scm.iv.repository.ProductStockRepository;
import org.ever._4ever_be_scm.scm.iv.repository.ProductRepository;
import org.ever._4ever_be_scm.scm.iv.repository.WarehouseRepository;
import org.ever._4ever_be_scm.scm.iv.repository.SupplierUserRepository;
import org.ever._4ever_be_scm.scm.iv.repository.*;
import org.ever._4ever_be_scm.scm.iv.service.InventoryService;
import org.ever._4ever_be_scm.scm.iv.vo.InventoryFilterVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static org.ever._4ever_be_scm.common.exception.ErrorCode.PRODUCT_NOT_FOUND;

/**
 * 재고 관리 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryServiceImpl implements InventoryService {

    private final ProductStockRepository productStockRepository;
    private final ProductStockLogRepository productStockLogRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    /**
     * 재고 목록 조회 (필터링 포함)
     */
    @Override
    public PagedResponseDto<InventoryItemDto> getInventoryItemsWithFilters(String type, String keyword, String statusCode, Integer page, Integer size) {
        // Repository에서 필터링된 결과를 가져옴
        Page<ProductStock> productStocks = productStockRepository.findWithFilters(
            type, 
            keyword, 
            statusCode, 
            PageRequest.of(page, size)
        );
        
        List<InventoryItemDto> items = productStocks.getContent().stream()
                .map(this::mapToInventoryItemDto)
                .collect(Collectors.toList());
        
        PageResponseDto pageInfo = PageResponseDto.builder()
                .number(page)
                .size(size)
                .totalElements((int) productStocks.getTotalElements())
                .totalPages(productStocks.getTotalPages())
                .hasNext(productStocks.hasNext())
                .build();
        
        return PagedResponseDto.<InventoryItemDto>builder()
                .content(items)
                .page(pageInfo)
                .build();
    }

    /**
     * 재고 상세 정보 조회
     * 
     * @param itemId 재고 ID
     * @return 재고 상세 정보
     */
    @Override
    public InventoryItemDetailDto getInventoryItemDetail(String itemId) {
        ProductStock productStock = productStockRepository.findByProductId(itemId)
                .orElseThrow(() -> new BusinessException(PRODUCT_NOT_FOUND));

        Product product = productStock.getProduct();
        Warehouse warehouse = productStock.getWarehouse();
        SupplierCompany supplierCompany = product.getSupplierCompany();

        // 이 제품의 재고 이동 내역 조회
        List<ProductStockLog> stockLogs = productStockLogRepository.findByProductId(product.getId());

        ProductStockLog latestLog = stockLogs.isEmpty() ? null : stockLogs.get(0);
        List<StockMovementDto> stockMovements = stockLogs.stream()
                .map(this::mapToStockMovementDto)
                .collect(Collectors.toList());

        return InventoryItemDetailDto.builder()
                // 제품 정보
                .itemId(product.getId())
                .itemName(product.getProductName())
                .itemNumber(product.getProductCode())
                .category(product.getCategory())
                // 재고 정보
                .currentStock(productStock.getTotalCount().intValue())
                .uomName(product.getUnit())
                .unitPrice(product.getOriginPrice())
                .totalAmount(productStock.getTotalCount().multiply(product.getOriginPrice()))
                .safetyStock(productStock.getSafetyCount().intValue())
                .statusCode(productStock.getStatus())
                // 위치 정보
                .warehouseId(warehouse.getId())
                .warehouseName(warehouse.getWarehouseName())
                .warehouseNumber(warehouse.getWarehouseCode())
                .location(warehouse.getLocation())
                .lastModified(latestLog != null ? latestLog.getCreatedAt() : null)
                // 공급사 이름
                .supplierCompanyName(supplierCompany.getCompanyName())
                // 재고 이동 내역
                .stockMovement(stockMovements)
                .build();
    }

    /**
     * 부족 재고 목록 조회
     * 
     * @param status 상태 필터 (주의, 위험)
     * @param pageable 페이징 정보
     * @return 부족 재고 목록
     */
    @Override
    public Page<ShortageItemDto> getShortageItems(String status, Pageable pageable) {
        Page<ProductStock> shortageItems;
        
        if (status != null && !status.isEmpty()) {
            shortageItems = productStockRepository.findShortageItems(status, pageable);
        } else {
            shortageItems = productStockRepository.findAllShortageItems(pageable);
        }
        
        return shortageItems.map(this::mapToShortageItemDto);
    }

    /**
     * 부족 재고 간단 정보 조회
     * 
     * @param pageable 페이징 정보
     * @return 부족 재고 간단 정보 목록
     */
    @Override
    public Page<ShortageItemPreviewDto> getShortageItemsPreview(Pageable pageable) {
        Page<ProductStock> shortageItems = productStockRepository.findAllShortageItems(pageable);
        
        return shortageItems.map(this::mapToShortageItemPreviewDto);
    }
    
    /**
     * ProductStock 엔티티를 InventoryItemDto로 변환
     */
    private InventoryItemDto mapToInventoryItemDto(ProductStock productStock) {
        Product product = productStock.getProduct();
        Warehouse warehouse = productStock.getWarehouse();

        BigDecimal totalPrice = productStock.getTotalCount().multiply(product.getOriginPrice());

        return InventoryItemDto.builder()
                .itemId(product.getId())
                .itemNumber(product.getProductCode())
                .itemName(product.getProductName())
                .category(product.getCategory())
                .currentStock(productStock.getTotalCount().intValue())
                .safetyStock(productStock.getSafetyCount().intValue())
                .uomName(product.getUnit())
                .unitPrice(product.getOriginPrice())
                .totalAmount(totalPrice)
                .warehouseName(warehouse.getWarehouseName())
                .warehouseType(warehouse.getWarehouseType())
                .statusCode(productStock.getStatus())
                .build();
    }
    
    /**
     * ProductStockLog 엔티티를 StockMovementDto로 변환
     */
    private StockMovementDto mapToStockMovementDto(ProductStockLog stockLog) {

        //todo user연결하면 수정
        String createdByName = "메롱";

        // 창고 코드 결정 (이동 방향에 따라)
        String toWarehouseCode = null;
        String formWarehouseCode = null;
        if (stockLog.getToWarehouse() != null) {
            toWarehouseCode = stockLog.getToWarehouse().getWarehouseName()+" ("+ stockLog.getToWarehouse().getWarehouseCode()+")";
        }
        if (stockLog.getFromWarehouse() != null) {
            formWarehouseCode = stockLog.getFromWarehouse().getWarehouseName()+" ("+stockLog.getFromWarehouse().getWarehouseCode()+")";
        }
        
        return StockMovementDto.builder()
                .type(stockLog.getMovementType())
                .quantity(stockLog.getChangeCount().intValue())
                .uomName(stockLog.getProductStock().getProduct().getUnit())
                .movementDate(stockLog.getCreatedAt())
                .managerName(createdByName)
                .to(toWarehouseCode)
                .from(formWarehouseCode)
                .referenceNumber(stockLog.getReferenceCode())
                .note(null)
                .build();
    }
    
    /**
     * ProductStock 엔티티를 ShortageItemDto로 변환
     */
    private ShortageItemDto mapToShortageItemDto(ProductStock productStock) {
        Product product = productStock.getProduct();
        Warehouse warehouse = productStock.getWarehouse();
        
        int currentStock = productStock.getTotalCount().intValue();
        int safetyStock = productStock.getSafetyCount().intValue();
        BigDecimal totalPrice = productStock.getTotalCount().multiply(product.getOriginPrice());
        
        return ShortageItemDto.builder()
                .itemId(product.getId())
                .itemNumber(product.getProductCode())
                .itemName(product.getProductName())
                .category(product.getCategory())
                .warehouseName(warehouse.getWarehouseName())
                .warehouseNumber(warehouse.getWarehouseCode())
                .currentStock(currentStock)
                .safetyStock(safetyStock)
                .unitPrice(product.getOriginPrice())
                .totalAmount(totalPrice)
                .uomName(product.getUnit())
                .status(productStock.getStatus())
                .build();
    }
    
    /**
     * ProductStock 엔티티를 ShortageItemPreviewDto로 변환
     */
    private ShortageItemPreviewDto mapToShortageItemPreviewDto(ProductStock productStock) {
        Product product = productStock.getProduct();
        
        int currentStock = productStock.getTotalCount().intValue();
        int safetyStock = productStock.getSafetyCount().intValue();
        
        return ShortageItemPreviewDto.builder()
                .itemId(product.getId())
                .itemName(product.getProductName())
                .uomName(product.getUnit())
                .currentStock(currentStock)
                .safetyStock(safetyStock)
                .statusCode(productStock.getStatus())
                .build();
    }

    /**
     * 재고 추가
     */
    @Override
    @Transactional
    public void addInventoryItem(AddInventoryItemRequest request) {
        // Product 존재 확인
        Product product = productRepository.findById(request.getItemId())
                .orElseThrow(() -> new BusinessException(PRODUCT_NOT_FOUND));
        
        // Warehouse 존재 확인
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("창고를 찾을 수 없습니다."));
        
        // ProductStock 중복 확인
        if (productStockRepository.findByProductIdAndWarehouseId(request.getItemId(), request.getWarehouseId()).isPresent()) {
            throw new RuntimeException("이미 해당 창고에 재고가 존재합니다.");
        }
        
        // ProductStock 생성
        ProductStock productStock = ProductStock.builder()
                .product(product)
                .warehouse(warehouse)
                .totalCount(BigDecimal.valueOf(request.getCurrentStock()))
                .availableCount(BigDecimal.valueOf(request.getCurrentStock()))
                .safetyCount(BigDecimal.valueOf(request.getSafetyStock()))
                .status(calculateStatus(request.getCurrentStock(), request.getSafetyStock()))
                .build();
        
        productStockRepository.save(productStock);
    }

    /**
     * 안전재고 수정
     */
    @Override
    @Transactional
    public void updateSafetyStock(String itemId, Integer safetyStock) {
        List<ProductStock> productStocks = productStockRepository.findByListProductId(itemId);
        
        if (productStocks.isEmpty()) {
            throw new RuntimeException("해당 제품의 재고를 찾을 수 없습니다.");
        }
        
        // 모든 창고의 해당 제품 안전재고 업데이트
        for (ProductStock productStock : productStocks) {
            productStock.setSafetyCount(BigDecimal.valueOf(safetyStock));
            // 상태 재계산
            productStock.setStatus(calculateStatus(productStock.getTotalCount().intValue(), safetyStock));
            productStockRepository.save(productStock);
        }
    }

    /**
     * 재고 상태 계산
     */
    private String calculateStatus(Integer currentStock, Integer safetyStock) {
        if (currentStock == null || safetyStock == null || safetyStock == 0) {
            return "NORMAL";
        }
        
        double ratio = (double) currentStock / safetyStock;
        
        if (ratio >= 1.0) {
            return "NORMAL";  // 안전재고 이상
        } else if (ratio >= 0.7) {
            return "CAUTION"; // 안전재고의 70% 이상 ~ 100% 미만
        } else {
            return "URGENT";  // 안전재고의 70% 미만
        }
    }
}
