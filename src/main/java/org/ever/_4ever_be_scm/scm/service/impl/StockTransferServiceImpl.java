package org.ever._4ever_be_scm.scm.service.impl;

import lombok.RequiredArgsConstructor;
import org.ever._4ever_be_scm.scm.dto.StockTransferDetailDto;
import org.ever._4ever_be_scm.scm.dto.StockTransferDto;
import org.ever._4ever_be_scm.scm.entity.ProductStockLog;
import org.ever._4ever_be_scm.scm.repository.ProductStockLogRepository;
import org.ever._4ever_be_scm.scm.service.StockTransferService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 재고 이동 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockTransferServiceImpl implements StockTransferService {

    private final ProductStockLogRepository productStockLogRepository;
    
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
     * 재고 이동 상세 목록 조회
     * 
     * @param pageable 페이징 정보
     * @return 재고 이동 상세 목록
     */
    @Override
    public Page<StockTransferDetailDto> getStockTransfersDetail(Pageable pageable) {
        Page<ProductStockLog> stockLogs = productStockLogRepository.findAllStockMovements(pageable);
        return stockLogs.map(this::mapToStockTransferDetailDto);
    }
    
    /**
     * ProductStockLog 엔티티를 StockTransferDto로 변환
     */
    private StockTransferDto mapToStockTransferDto(ProductStockLog stockLog) {
        //todo user연결하면 수정
        String createdByName = "메롱";
        return StockTransferDto.builder()
                .type(stockLog.getMovementType())
                .quantity(stockLog.getChangeCount().intValue())
                .unit(stockLog.getProductStock().getProduct().getUnit())
                .itemName(stockLog.getProductStock().getProduct().getProductName())
                .workTime(stockLog.getCreatedAt())
                .manager(createdByName)
                .build();
    }
    
    /**
     * ProductStockLog 엔티티를 StockTransferDetailDto로 변환
     */
    private StockTransferDetailDto mapToStockTransferDetailDto(ProductStockLog stockLog) {

        //todo user연결하면 수정
        String createdByName = "메롱";
        
        return StockTransferDetailDto.builder()
                .type(stockLog.getMovementType())
                .quantity(stockLog.getChangeCount().intValue())
                .unit(stockLog.getProductStock().getProduct().getUnit())
                .itemName(stockLog.getProductStock().getProduct().getProductName())
                .workTime(stockLog.getCreatedAt())
                .manager(createdByName)
                .warehouseCode(stockLog.getToWarehouse().getWarehouseCode())
                .referenceCode(stockLog.getReferenceCode())
                .build();
    }
}
