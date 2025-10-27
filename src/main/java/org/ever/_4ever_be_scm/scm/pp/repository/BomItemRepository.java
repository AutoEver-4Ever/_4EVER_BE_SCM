package org.ever._4ever_be_scm.scm.pp.repository;

import org.ever._4ever_be_scm.scm.pp.entity.BomItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BomItemRepository extends JpaRepository<BomItem, String> {
    Optional<BomItem> findByBomIdAndComponentId(String bomId, String componentId);
    
    List<BomItem> findByBomId(String bomId);
    
    @Modifying
    @Query("DELETE FROM BomItem b WHERE b.bomId = :bomId")
    void deleteByBomId(@Param("bomId") String bomId);
}
