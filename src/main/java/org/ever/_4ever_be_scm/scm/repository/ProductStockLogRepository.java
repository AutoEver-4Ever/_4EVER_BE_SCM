package org.ever._4ever_be_scm.scm.repository;

import org.ever._4ever_be_scm.scm.entity.ProductStockLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductStockLogRepository extends JpaRepository<ProductStockLog, Long> {
    
    @Query("SELECT psl FROM ProductStockLog psl " +
           "JOIN FETCH psl.productStock ps " +
           "JOIN FETCH ps.product p " +
           "ORDER BY psl.createdAt DESC")
    Page<ProductStockLog> findAllStockMovements(Pageable pageable);
    
    @Query("SELECT psl FROM ProductStockLog psl " +
           "JOIN FETCH psl.productStock ps " +
           "JOIN FETCH ps.product p " +
           "WHERE p.id = :productId " +
           "ORDER BY psl.createdAt DESC")
    List<ProductStockLog> findByProductId(@Param("productId") String productId);

}
