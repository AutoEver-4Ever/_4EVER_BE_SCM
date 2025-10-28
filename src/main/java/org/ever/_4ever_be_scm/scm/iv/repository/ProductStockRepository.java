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
}
