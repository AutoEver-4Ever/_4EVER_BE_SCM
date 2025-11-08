package org.ever._4ever_be_scm.scm.pp.repository;

import org.ever._4ever_be_scm.scm.pp.entity.Mrp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MrpRepository extends JpaRepository<Mrp, String> {
    List<Mrp> findByQuotationId(String quotationId);
    List<Mrp> findByQuotationIdIn(List<String> quotationIds);
    List<Mrp> findByBomId(String bomId);
    List<Mrp> findByQuotationIdAndProductId(String quotationId, String productId);

    @Query("SELECT DISTINCT m.quotationId FROM Mrp m WHERE m.quotationId IS NOT NULL")
    List<String> findDistinctQuotationIds();
}
