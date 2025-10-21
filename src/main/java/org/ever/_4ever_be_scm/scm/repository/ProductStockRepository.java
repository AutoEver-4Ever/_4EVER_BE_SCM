package org.ever._4ever_be_scm.scm.repository;

import org.ever._4ever_be_scm.scm.entity.ProductStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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
           "WHERE ps.totalCount < ps.safetyCount " +
           "AND (:status IS NULL OR ps.status = :status)")
    Page<ProductStock> findShortageItems(@Param("status") String status, Pageable pageable);

    @Query("SELECT ps FROM ProductStock ps " +
           "JOIN FETCH ps.product p " +
           "JOIN FETCH ps.warehouse w " +
           "WHERE ps.totalCount < ps.safetyCount")
    Page<ProductStock> findAllShortageItems(Pageable pageable);

    @Query("SELECT COUNT(ps) FROM ProductStock ps WHERE ps.totalCount < ps.safetyCount AND ps.status = :status")
    long countShortageItemsByStatus(@Param("status") String status);
}
