package org.ever._4ever_be_scm.scm.mm.repository;

import org.ever._4ever_be_scm.scm.mm.entity.ProductOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ProductOrderRepository extends JpaRepository<ProductOrder, String> {
    Page<ProductOrder> findByApprovalId_ApprovalStatus(String approvalIdApprovalStatus,Pageable pageable);
}
