package org.ever._4ever_be_scm.scm.pp.repository;

import org.ever._4ever_be_scm.scm.pp.entity.Mes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MesRepository extends JpaRepository<Mes, String> {

    Optional<Mes> findByQuotationId(String quotationId);

    List<Mes> findByQuotationIdIn(List<String> quotationIds);

    @Query("SELECT m FROM Mes m WHERE " +
            "(:quotationId IS NULL OR :quotationId = '' OR m.quotationId = :quotationId) AND " +
            "(:status IS NULL OR :status = '' OR :status = 'ALL' OR m.status = :status)")
    Page<Mes> findWithFilters(
            @Param("quotationId") String quotationId,
            @Param("status") String status,
            Pageable pageable
    );

    List<Mes> findByStatusOrderByCreatedAtDesc(String status);

    List<Mes> findByStatusAndUpdatedAtBetween(String status, LocalDateTime startDate, LocalDateTime endDate);
}
