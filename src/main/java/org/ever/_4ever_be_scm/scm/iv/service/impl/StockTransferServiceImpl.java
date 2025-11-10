package org.ever._4ever_be_scm.scm.iv.service.impl;

import lombok.RequiredArgsConstructor;
import org.ever._4ever_be_scm.scm.iv.dto.StockDeliveryRequestDto;
import org.ever._4ever_be_scm.scm.iv.dto.StockTransferDto;
import org.ever._4ever_be_scm.scm.iv.dto.StockTransferRequestDto;
import org.ever._4ever_be_scm.scm.iv.entity.ProductStock;
import org.ever._4ever_be_scm.scm.iv.entity.ProductStockLog;
import org.ever._4ever_be_scm.scm.iv.entity.Warehouse;
import org.ever._4ever_be_scm.scm.iv.repository.ProductStockLogRepository;
import org.ever._4ever_be_scm.scm.iv.repository.ProductStockRepository;
import org.ever._4ever_be_scm.scm.iv.repository.WarehouseRepository;
import org.ever._4ever_be_scm.scm.iv.service.StockTransferService;
import org.ever._4ever_be_scm.scm.mm.integration.dto.InternalUserResponseDto;
import org.ever._4ever_be_scm.scm.mm.integration.port.InternalUserServicePort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 재고 이동 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockTransferServiceImpl implements StockTransferService {

    private final ProductStockLogRepository productStockLogRepository;
    private final ProductStockRepository productStockRepository;
    private final WarehouseRepository warehouseRepository;
    private final InternalUserServicePort  internalUserServicePort;
    
    /**
     * 재고 이동 목록 조회
     * 
     * @param pageable 페이징 정보
     * @return 재고 이동 목록
     */
    @Override
    public Page<StockTransferDto> getStockTransfers(Pageable pageable) {
        Page<ProductStockLog> stockLogs = productStockLogRepository.findAllStockMovements(pageable);
        return stockLogs.map(this::mapToStockTransferDto);
    }
    
    /**
     * 창고간 재고 이동 생성
     * 
     * @param request 재고 이동 요청 정보
     */
    @Override
    @Transactional
    public void createStockTransfer(StockTransferRequestDto request, String requesterId) {
        // 1. 현재 productStock 조회 (itemId로 Product 찾기)
        ProductStock currentStock = productStockRepository.findByProductId(request.getItemId())
                .orElseThrow(() -> new RuntimeException("해당 제품의 재고를 찾을 수 없습니다."));

        // 재고 이동 시 예약재고를 제외한 실제 가용재고로 체크
        BigDecimal actualAvailable = currentStock.getActualAvailableCount();
        if (actualAvailable.compareTo(request.getStockQuantity()) < 0) {
            throw new RuntimeException(String.format("이동할 수량이 실제 가용재고보다 많습니다. (요청: %s, 가용: %s, 예약재고: %s)",
                request.getStockQuantity(), actualAvailable, currentStock.getReservedCount()));
        }

        
        // 3. 출발 창고와 도착 창고 조회
        Warehouse fromWarehouse = warehouseRepository.findById(request.getFromWarehouseId())
                .orElseThrow(() -> new RuntimeException("출발 창고를 찾을 수 없습니다."));

        if(!fromWarehouse.getId().equals(currentStock.getWarehouse().getId())) {
            throw new RuntimeException("출발 창고가 다릅니다");
        }

        if(!fromWarehouse.getInternalUserId().equals(requesterId)){
            throw new RuntimeException("담당자가 아닙니다.");
        }
        
        Warehouse toWarehouse = warehouseRepository.findById(request.getToWarehouseId())
                .orElseThrow(() -> new RuntimeException("도착 창고를 찾을 수 없습니다."));
        
        // 4. ProductStock의 창고 변경 (예약재고 정보 유지)
        BigDecimal previousCount = currentStock.getAvailableCount();
        currentStock = ProductStock.builder()
                .id(currentStock.getId())
                .product(currentStock.getProduct())
                .warehouse(toWarehouse) // 창고 변경
                .availableCount(currentStock.getAvailableCount())
                .safetyCount(currentStock.getSafetyCount())
                .reservedCount(currentStock.getReservedCount()) // 예약재고 정보 유지
                .status(currentStock.getStatus())
                .build();

        productStockRepository.save(currentStock);
        
        // 5. ProductStockLog 생성
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String trCode = "TR-" + uuid.substring(uuid.length() - 6);

        ProductStockLog stockLog = ProductStockLog.builder()
                .productStock(currentStock)
                .movementType("이동")
                .changeCount(request.getStockQuantity())
                .previousCount(previousCount)
                .currentCount(request.getStockQuantity())
                .fromWarehouse(fromWarehouse)
                .toWarehouse(toWarehouse)
                .createdById(requesterId) // 임의의 담당자 ID
                .referenceCode(trCode)
                .note(request.getReason())
                .build();
        
        productStockLogRepository.save(stockLog);
    }
    
    /**
     * 재고 입출고 처리 (requesterId 포함)
     *
     * @param productId 제품 ID
     * @param quantity 입출고 수량 (양수: 입고, 음수: 출고)
     * @param requesterId 요청자 ID
     * @param referenceCode 참조 코드
     * @param reason 사유
     */
    @Override
    @Transactional
    public void processStockDelivery(String productId, BigDecimal quantity, String requesterId,
                                      String referenceCode, String reason) {
        // 1. 현재 productStock 조회 (itemId로 Product 찾기)
        ProductStock currentStock = productStockRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("해당 제품의 재고를 찾을 수 없습니다."));

        // 2. 이전 수량 저장
        BigDecimal previousCount = currentStock.getAvailableCount();

        // 3. 입고/출고에 따른 수량 변경
        BigDecimal newTotalCount;
        BigDecimal newAvailableCount;
        String movementType;
        Warehouse fromWarehouse = null;
        Warehouse toWarehouse = null;

        // 담당자 결정: system이면 창고 담당자, 아니면 요청자
        String createdBy = requesterId.equals("system")
                ? currentStock.getWarehouse().getInternalUserId()
                : requesterId;

        if (quantity.compareTo(BigDecimal.ZERO) > 0) {
            // 입고 (quantity > 0):
            newTotalCount = currentStock.getAvailableCount().add(quantity);
            newAvailableCount = currentStock.getAvailableCount().add(quantity);
            movementType = "입고";
            toWarehouse = currentStock.getWarehouse(); // 현재 창고로 입고
        } else {
            // 출고 (quantity < 0): 요청자가 처리
            BigDecimal absQuantity = quantity.abs();
            // 예약재고를 제외한 실제 가용재고로 체크
            BigDecimal actualAvailable = currentStock.getActualAvailableCount();
            if (actualAvailable.compareTo(absQuantity) < 0) {
                throw new RuntimeException(String.format("출고할 수량이 실제 가용재고보다 많습니다. (요청: %s, 가용: %s, 예약재고: %s)",
                        absQuantity, actualAvailable, currentStock.getReservedCount()));
            }
            newTotalCount = currentStock.getAvailableCount().subtract(absQuantity);
            newAvailableCount = currentStock.getAvailableCount().subtract(absQuantity);
            movementType = "출고";
            fromWarehouse = currentStock.getWarehouse(); // 현재 창고에서 출고
        }

        String status = calculateStatus(newAvailableCount.intValue(),currentStock.getSafetyCount().intValue());

        // 4. ProductStock 업데이트 (예약재고 정보 유지)
        ProductStock updatedStock = ProductStock.builder()
                .id(currentStock.getId())
                .product(currentStock.getProduct())
                .warehouse(currentStock.getWarehouse())
                .availableCount(newAvailableCount)
                .safetyCount(currentStock.getSafetyCount())
                .reservedCount(currentStock.getReservedCount()) // 예약재고 정보 유지
                .forShipmentCount(currentStock.getForShipmentCount()) // forShipmentCount 유지
                .status(status)
                .build();

        productStockRepository.save(updatedStock);

        // 5. ProductStockLog 생성
        ProductStockLog stockLog = ProductStockLog.builder()
                .productStock(updatedStock)
                .movementType(movementType)
                .changeCount(quantity.abs())
                .previousCount(previousCount)
                .currentCount(newTotalCount)
                .fromWarehouse(fromWarehouse)
                .toWarehouse(toWarehouse)
                .createdById(createdBy) // 입고: 창고 담당자, 출고: 요청자
                .referenceCode(referenceCode)
                .note(reason)
                .build();

        productStockLogRepository.save(stockLog);
    }

    private StockTransferDto mapToStockTransferDto(ProductStockLog stockLog) {

        InternalUserResponseDto managerInfo = internalUserServicePort.getInternalUserInfoById(stockLog.getCreatedById());

        return StockTransferDto.builder()
                .type(stockLog.getMovementType())
                .quantity(stockLog.getChangeCount().intValue())
                .uomName(stockLog.getProductStock().getProduct().getUnit())
                .itemName(stockLog.getProductStock().getProduct().getProductName())
                .workDate(stockLog.getCreatedAt())
                .managerName(managerInfo.getName())
                .build();
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
