package org.ever._4ever_be_scm.scm.service.impl;

import lombok.RequiredArgsConstructor;
import org.ever._4ever_be_scm.common.exception.BusinessException;
import org.ever._4ever_be_scm.scm.dto.InventoryItemDetailDto;
import org.ever._4ever_be_scm.scm.dto.InventoryItemDto;
import org.ever._4ever_be_scm.scm.dto.ShortageItemDto;
import org.ever._4ever_be_scm.scm.dto.ShortageItemPreviewDto;
import org.ever._4ever_be_scm.scm.dto.StockMovementDto;
import org.ever._4ever_be_scm.scm.entity.*;
import org.ever._4ever_be_scm.scm.repository.*;
import org.ever._4ever_be_scm.scm.service.InventoryService;
import org.ever._4ever_be_scm.scm.vo.InventoryFilterVo;
import org.springframework.data.domain.Page;
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
    private final SupplierUserRepository supplierUserRepository;

    /**
     * 재고 목록 조회
     * 
     * @param filterVo 필터 조건
     * @param pageable 페이징 정보
     * @return 재고 목록
     */
    @Override
    public Page<InventoryItemDto> getInventoryItems(InventoryFilterVo filterVo, Pageable pageable) {
        // 필터 조건으로 데이터베이스 조회
        Page<ProductStock> productStocks;

        String itemName = (filterVo.getItemName() == null) ? "" : filterVo.getItemName();

        if (filterVo != null) {
            productStocks = productStockRepository.findByFilters(
                filterVo.getCategory(),
                filterVo.getStatus(),
                filterVo.getWarehouseId(),
                    itemName,
                pageable
            );
        } else {
            productStocks = productStockRepository.findAll(pageable);
        }
        
        // ProductStock을 InventoryItemDto로 변환
        return productStocks.map(this::mapToInventoryItemDto);
    }

    /**
     * 재고 상세 정보 조회
     * 
     * @param itemId 재고 ID
     * @return 재고 상세 정보
     */
    @Override
    public InventoryItemDetailDto getInventoryItemDetail(String itemId) {
        ProductStock productStock = productStockRepository.findById(itemId)
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
                .productId(product.getId())
                .productName(product.getProductName())
                .productCode(product.getProductCode())
                .category(product.getCategory())
                // 재고 정보
                .currentStock(productStock.getTotalCount().intValue())
                .unit(product.getUnit())
                .price(product.getOriginPrice())
                .totalValue(productStock.getTotalCount().multiply(product.getOriginPrice()))
                .safetyStock(productStock.getSafetyCount().intValue())
                .status(productStock.getStatus())
                // 위치 정보
                .warehouseName(warehouse.getWarehouseName())
                .warehouseCode(warehouse.getWarehouseCode())
                .location(warehouse.getLocation())
                .latestLog(latestLog != null ? latestLog.getCreatedAt() : null)
                // 공급사 이름
                .supplierName(supplierCompany.getCompanyName())
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
                .productId(product.getId())
                .productCode(product.getProductCode())
                .productName(product.getProductName())
                .category(product.getCategory())
                .currentStock(productStock.getTotalCount().intValue())
                .safetyStock(productStock.getSafetyCount().intValue())
                .unit(product.getUnit())
                .price(product.getOriginPrice())
                .totalValue(totalPrice)
                .warehouseName(warehouse.getWarehouseName())
                .warehouseType(warehouse.getWarehouseType())
                .warehouseCode(warehouse.getWarehouseCode())
                .status(productStock.getStatus())
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
                .unit(stockLog.getProductStock().getProduct().getUnit())
                .date(stockLog.getCreatedAt())
                .manager(createdByName)
                .toWarehouseCode(toWarehouseCode)
                .fromWarehouseCode(formWarehouseCode)
                .referenceCode(stockLog.getReferenceCode())
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
        int shortageAmount = Math.max(0, safetyStock - currentStock);
        BigDecimal totalPrice = productStock.getTotalCount().multiply(product.getOriginPrice());
        
        return ShortageItemDto.builder()
                .productId(product.getId())
                .productCode(product.getProductCode())
                .productName(product.getProductName())
                .category(product.getCategory())
                .warehouseName(warehouse.getWarehouseName())
                .warehouseCode(warehouse.getWarehouseCode())
                .currentStock(currentStock)
                .safetyStock(safetyStock)
                .price(product.getOriginPrice())
                .totalValue(totalPrice)
                .unit(product.getUnit())
                .shortageAmount(shortageAmount)
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
        int shortageAmount = Math.max(0, safetyStock - currentStock);
        
        return ShortageItemPreviewDto.builder()
                .productId(product.getId())
                .productName(product.getProductName())
                .unit(product.getUnit())
                .stockQuantity(currentStock)
                .safetyStock(safetyStock)
                .shortageAmount(shortageAmount)
                .status(productStock.getStatus())
                .build();
    }
}
