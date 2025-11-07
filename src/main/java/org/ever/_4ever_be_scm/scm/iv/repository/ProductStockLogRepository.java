package org.ever._4ever_be_scm.scm.iv.repository;

import org.ever._4ever_be_scm.scm.iv.entity.ProductStockLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductStockLogRepository extends JpaRepository<ProductStockLog, String> {
    
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

    /**
     * 최근 입출고 이력 조회
     *
     * @param movementType 입출고 타입 (예: 입고, 출고)
     * @param createdById 담당자 ID
     * @param pageable 페이징 정보
     * @return 입출고 이력 목록
     */
    @EntityGraph(attributePaths = {"productStock", "productStock.product", "productStock.warehouse"})
    List<ProductStockLog> findByMovementTypeAndCreatedByIdOrderByCreatedAtDesc(
            String movementType,
            String createdById,
            Pageable pageable);

}
