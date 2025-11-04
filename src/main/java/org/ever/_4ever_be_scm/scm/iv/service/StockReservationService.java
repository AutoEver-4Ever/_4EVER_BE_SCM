package org.ever._4ever_be_scm.scm.iv.service;

import java.math.BigDecimal;
import java.util.List;

public interface StockReservationService {
    
    /**
     * 재고 예약
     */
    boolean reserveStock(String productId, BigDecimal quantity);
    
    /**
     * 재고 예약 해제
     */
    void releaseReservation(String productId, BigDecimal quantity);
    
    /**
     * 예약된 재고를 실제로 차감
     */
    void consumeReservedStock(String productId, BigDecimal quantity);
    
    /**
     * 여러 제품의 재고를 한번에 예약
     */
    boolean reserveMultipleStocks(List<StockReservationRequest> requests);
    
    /**
     * 여러 제품의 재고 예약을 한번에 해제
     */
    void releaseMultipleReservations(List<StockReservationRequest> requests);
    
    /**
     * 재고 예약 요청 클래스
     */
    public static class StockReservationRequest {
        private String productId;
        private BigDecimal quantity;
        
        public StockReservationRequest(String productId, BigDecimal quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }
        
        public String getProductId() { return productId; }
        public BigDecimal getQuantity() { return quantity; }
    }
}
