package org.ever._4ever_be_scm.scm.pp.repository;

import org.ever._4ever_be_scm.scm.pp.entity.Routing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoutingRepository extends JpaRepository<Routing, String> {
    Optional<Routing> findByBomItemId(String bomItemId);
    
    @Modifying
    @Query("DELETE FROM Routing r WHERE r.bomItemId IN (SELECT b.id FROM BomItem b WHERE b.bomId = :bomId)")
    void deleteByBomId(@Param("bomId") String bomId);
}
