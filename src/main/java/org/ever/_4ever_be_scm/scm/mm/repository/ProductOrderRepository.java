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

    @Query("SELECT SUM(p.totalPrice) FROM ProductOrder p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    Optional<BigDecimal> sumTotalPriceByOrderDateBetween(@Param("startDate") LocalDateTime startDate,
                                                         @Param("endDate") LocalDateTime endDate);
}
