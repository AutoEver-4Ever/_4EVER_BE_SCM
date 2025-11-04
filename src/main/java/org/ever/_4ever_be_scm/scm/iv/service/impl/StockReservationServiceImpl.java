package org.ever._4ever_be_scm.scm.iv.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ever._4ever_be_scm.scm.iv.entity.ProductStock;
import org.ever._4ever_be_scm.scm.iv.repository.ProductStockRepository;
import org.ever._4ever_be_scm.scm.iv.service.StockReservationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockReservationServiceImpl implements StockReservationService {
    
    private final ProductStockRepository productStockRepository;
    
    @Override
    @Transactional
    public boolean reserveStock(String productId, BigDecimal quantity) {
        Optional<ProductStock> stockOpt = productStockRepository.findByProductId(productId);
        if (stockOpt.isEmpty()) {
            log.warn("재고를 찾을 수 없습니다: productId={}", productId);
            return false;
        }
        
        ProductStock stock = stockOpt.get();
        boolean success = stock.reserveStock(quantity);
        
        if (success) {
            productStockRepository.save(stock);
            log.info("재고 예약 성공: productId={}, quantity={}", productId, quantity);
        } else {
            log.warn("재고 예약 실패 (재고 부족): productId={}, 요청={}, 가용={}", 
                    productId, quantity, stock.getActualAvailableCount());
        }
        
        return success;
    }
    
    @Override
    @Transactional
    public void releaseReservation(String productId, BigDecimal quantity) {
        Optional<ProductStock> stockOpt = productStockRepository.findByProductId(productId);
        if (stockOpt.isEmpty()) {
            log.warn("재고를 찾을 수 없습니다: productId={}", productId);
            return;
        }
        
        ProductStock stock = stockOpt.get();
        stock.releaseReservation(quantity);
        productStockRepository.save(stock);
        
        log.info("재고 예약 해제: productId={}, quantity={}", productId, quantity);
    }
    
    @Override
    @Transactional
    public void consumeReservedStock(String productId, BigDecimal quantity) {
        Optional<ProductStock> stockOpt = productStockRepository.findByProductId(productId);
        if (stockOpt.isEmpty()) {
            log.warn("재고를 찾을 수 없습니다: productId={}", productId);
            return;
        }
        
        ProductStock stock = stockOpt.get();
        stock.consumeReservedStock(quantity);
        productStockRepository.save(stock);
        
        log.info("예약 재고 차감: productId={}, quantity={}", productId, quantity);
    }
    
    @Override
    @Transactional
    public boolean reserveMultipleStocks(List<StockReservationRequest> requests) {
        // 모든 재고를 먼저 확인
        for (StockReservationRequest request : requests) {
            Optional<ProductStock> stockOpt = productStockRepository.findByProductId(request.getProductId());
            if (stockOpt.isEmpty() || 
                stockOpt.get().getActualAvailableCount().compareTo(request.getQuantity()) < 0) {
                log.warn("재고 부족으로 예약 실패: productId={}", request.getProductId());
                return false;
            }
        }
        
        // 모든 재고가 충족되면 예약 실행
        for (StockReservationRequest request : requests) {
            reserveStock(request.getProductId(), request.getQuantity());
        }
        
        return true;
    }
    
    @Override
    @Transactional
    public void releaseMultipleReservations(List<StockReservationRequest> requests) {
        for (StockReservationRequest request : requests) {
            releaseReservation(request.getProductId(), request.getQuantity());
        }
    }
}
