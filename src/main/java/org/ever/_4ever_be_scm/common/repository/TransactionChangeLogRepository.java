package org.ever._4ever_be_scm.common.repository;

import org.ever._4ever_be_scm.common.entity.TransactionChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 트랜잭션 변경 로그 레포지토리
 */
@Repository
public interface TransactionChangeLogRepository extends JpaRepository<TransactionChangeLog, Long> {
    /**
     * 트랜잭션 ID로 로그 조회
     * @param transactionId 트랜잭션 ID
     * @return 트랜잭션 로그
     */
    Optional<TransactionChangeLog> findByTransactionId(String transactionId);
    
    /**
     * 트랜잭션 보상 완료 표시
     * @param transactionId 트랜잭션 ID
     * @return 업데이트된 레코드 수
     */
    @Modifying
    @Query("UPDATE TransactionChangeLog t SET t.compensated = true WHERE t.transactionId = :transactionId")
    int markAsCompensated(@Param("transactionId") String transactionId);
    
    /**
     * 특정 날짜 이전의 보상 완료된 로그 삭제
     * @param beforeDate 기준 날짜
     * @return 삭제된 레코드 수
     */
    @Modifying
    @Query("DELETE FROM TransactionChangeLog t WHERE t.compensated = true AND t.timestamp < :beforeDate")
    int deleteCompensatedBefore(@Param("beforeDate") LocalDateTime beforeDate);
}
