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
    
    /**
     * 상태별 창고 개수 조회
     * 
     * @param status 창고 상태
     * @return 해당 상태의 창고 개수
     */
    long countByStatus(String status);
    
    /**
     * 날짜 범위와 상태별 창고 개수 조회
     * 
     * @param status 창고 상태
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 해당 조건의 창고 개수
     */
    long countByStatusAndCreatedAtBetween(String status, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    /**
     * 날짜 범위 내 총 창고 개수 조회
     * 
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 총 창고 개수
     */
    long countByCreatedAtBetween(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
}
