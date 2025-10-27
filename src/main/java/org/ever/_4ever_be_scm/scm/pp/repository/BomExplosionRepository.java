package org.ever._4ever_be_scm.scm.pp.repository;

import org.ever._4ever_be_scm.scm.pp.entity.BomExplosion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BomExplosionRepository extends JpaRepository<BomExplosion, String> {
    List<BomExplosion> findByParentBomId(String parentBomId);
    
    @Modifying
    @Query("DELETE FROM BomExplosion b WHERE b.parentBomId = :parentBomId")
    void deleteByParentBomId(@Param("parentBomId") String parentBomId);
}
