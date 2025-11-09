package org.ever._4ever_be_scm.scm.pp.repository;

import org.ever._4ever_be_scm.scm.pp.entity.Bom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface BomRepository extends JpaRepository<Bom, String> {
    Optional<Bom> findByProductId(String productId);

    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}
