package org.ever._4ever_be_scm.scm.mm.repository;

import org.ever._4ever_be_scm.scm.mm.entity.ProductOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;


@Repository
public interface ProductOrderRepository extends JpaRepository<ProductOrder, String> {
    Page<ProductOrder> findByApprovalId_ApprovalStatus(String approvalIdApprovalStatus,Pageable pageable);

    long countByApprovalId_ApprovalStatusAndCreatedAtBetween(String status, LocalDateTime startDate, LocalDateTime endDate);

    long countByApprovalId_ApprovalStatusAndUpdatedAtBetween(String status, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT SUM(p.totalPrice) FROM ProductOrder p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    Optional<BigDecimal> sumTotalPriceByOrderDateBetween(@Param("startDate") LocalDateTime startDate,
                                                         @Param("endDate") LocalDateTime endDate);
    
    /**
     * 상태별, dueDate 기준 날짜 범위로 ProductOrder 조회
     * 
     * @param approvalStatus 승인 상태
     * @param startDate 시작일
     * @param endDate 종료일
     * @param pageable 페이징 정보
     * @return ProductOrder 페이지
     */
    Page<ProductOrder> findByApprovalId_ApprovalStatusAndDueDateBetween(
            String approvalStatus, LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    /**
     * 승인 상태별 ProductOrder 개수 조회
     * 
     * @param approvalStatus 승인 상태
     * @return 해당 상태의 ProductOrder 개수
     */
    long countByApprovalId_ApprovalStatus(String approvalStatus);

    Page<ProductOrder> findBySupplierCompanyNameOrderByCreatedAtDesc(String supplierCompanyName, Pageable pageable);

    Page<ProductOrder> findAllByOrderByCreatedAtDesc(Pageable pageable);

    java.util.List<ProductOrder> findBySupplierCompanyNameOrderByCreatedAtDesc(String supplierCompanyName);

    java.util.List<ProductOrder> findAllByOrderByCreatedAtDesc();

    long countBySupplierCompanyNameAndCreatedAtBetween(String supplierCompanyName, LocalDateTime startDate, LocalDateTime endDate);
}
