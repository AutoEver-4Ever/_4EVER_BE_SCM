package org.ever._4ever_be_scm.scm.mm.repository;

import org.ever._4ever_be_scm.scm.mm.entity.ProductRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface ProductRequestRepository extends JpaRepository<ProductRequest, String> {
    long countByApprovalId_ApprovalStatusAndCreatedAtBetween(String pending, LocalDateTime startDate, LocalDateTime endDate);
    long countByCreatedAtBetween(LocalDateTime attr0, LocalDateTime attr1);

    Page<ProductRequest> findByRequesterIdOrderByCreatedAtDesc(String requesterId, Pageable pageable);

    Page<ProductRequest> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
