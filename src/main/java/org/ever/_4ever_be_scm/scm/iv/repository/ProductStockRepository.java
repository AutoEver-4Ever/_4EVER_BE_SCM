package org.ever._4ever_be_scm.scm.iv.repository;

import org.ever._4ever_be_scm.scm.iv.entity.ProductStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductStockRepository extends JpaRepository<ProductStock, String> {
    @Query("SELECT ps FROM ProductStock ps " +
           "JOIN FETCH ps.product p " +
           "JOIN FETCH ps.warehouse w " +
           "WHERE (:category IS NULL OR p.category = :category) " +
           "AND (:status IS NULL OR ps.status = :status) " +
           "AND (:warehouseId IS NULL OR w.id = :warehouseId) " +
           "AND (:itemName IS NULL OR p.productName LIKE CONCAT('%', :itemName, '%'))")
    Page<ProductStock> findByFilters(
            @Param("category") String category,
            @Param("status") String status,
            @Param("warehouseId") String warehouseId,
            @Param("itemName") String itemName,
            Pageable pageable);
    
    @Query("SELECT ps FROM ProductStock ps " +
           "JOIN FETCH ps.product p " +
           "JOIN FETCH ps.warehouse w " +
           "WHERE (:status IS NULL OR ps.status = :status)")
    Page<ProductStock> findShortageItems(@Param("status") String status, Pageable pageable);

    @Query("SELECT ps FROM ProductStock ps " +
           "JOIN FETCH ps.product p " +
           "JOIN FETCH ps.warehouse w " +
           "WHERE ps.availableCount < ps.safetyCount")
    Page<ProductStock> findAllShortageItems(Pageable pageable);
    
    @Query("SELECT ps FROM ProductStock ps WHERE ps.product.id = :productId")
    List<ProductStock> findByListProductId(@Param("productId") String productId);

    @Query("SELECT ps FROM ProductStock ps WHERE ps.product.id = :productId")
    Optional<ProductStock> findByProductId(@Param("productId") String productId);

    @Query("SELECT ps FROM ProductStock ps " +
           "JOIN FETCH ps.product p " +
           "JOIN FETCH ps.warehouse w " +
           "WHERE (:type IS NULL OR :keyword IS NULL OR :keyword = '' OR " +
           "       (:type = 'WAREHOUSE_NAME' AND w.warehouseName LIKE CONCAT('%', :keyword, '%')) OR " +
           "       (:type = 'ITEM_NAME' AND p.productName LIKE CONCAT('%', :keyword, '%'))) " +
           "AND (:statusCode IS NULL OR :statusCode = 'ALL' OR ps.status = :statusCode)")
    Page<ProductStock> findWithFilters(
            @Param("type") String type,
            @Param("keyword") String keyword,
            @Param("statusCode") String statusCode,
            Pageable pageable);

    Optional<Object> findByProductIdAndWarehouseId(String itemId, String warehouseId);
    
    /**
     * 상태별 ProductStock 개수 조회
     * 
     * @param status 상태
     * @return 해당 상태의 ProductStock 개수
     */
    long countByStatus(String status);
    
    /**
     * 날짜 범위와 상태별 ProductStock 개수 조회
     * 
     * @param status 상태
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 해당 조건의 ProductStock 개수
     */
    long countByStatusAndUpdatedAtBetween(String status, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    /**
     * 날짜 범위 내 총 재고 가치 계산
     * 
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 총 재고 가치
     */
    @Query("SELECT SUM(ps.availableCount * p.originPrice) FROM ProductStock ps JOIN ps.product p WHERE ps.createdAt BETWEEN :startDate AND :endDate")
    java.util.Optional<java.math.BigDecimal> sumTotalStockValueByDateBetween(@Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
}
