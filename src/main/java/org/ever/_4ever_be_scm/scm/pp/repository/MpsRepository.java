package org.ever._4ever_be_scm.scm.pp.repository;

import org.ever._4ever_be_scm.scm.pp.entity.Mps;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MpsRepository extends JpaRepository<Mps, String> {
    Optional<Mps> findByQuotationId(String quotationId);
    List<Mps> findByQuotationIdIn(List<String> quotationIds);
}
