package org.ever._4ever_be_scm.scm.pp.repository;

import org.ever._4ever_be_scm.scm.pp.entity.MrpRun;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MrpRunRepository extends JpaRepository<MrpRun, String> {
    List<MrpRun> findByQuotationId(String quotationId);
    List<MrpRun> findByProductId(String productId);
    List<MrpRun> findByProductIdAndStatus(String productId, String status);
    Page<MrpRun> findByStatus(String status, Pageable pageable);
}
