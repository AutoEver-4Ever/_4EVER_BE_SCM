package org.ever._4ever_be_scm.scm.pp.repository;

import org.ever._4ever_be_scm.scm.pp.entity.MrpRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MrpRunRepository extends JpaRepository<MrpRun, String> {
    List<MrpRun> findByQuotationId(String quotationId);
    List<MrpRun> findByMrpResultId(String mrpResultId);
}
