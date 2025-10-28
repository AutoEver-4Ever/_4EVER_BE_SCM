package org.ever._4ever_be_scm.scm.iv.repository;

import org.ever._4ever_be_scm.scm.iv.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, String> {
    
    /**
     * 상태별 창고 목록 조회
     * 
     * @param status 창고 상태
     * @return 창고 목록
     */
    List<Warehouse> findAllByStatus(String status);
}
