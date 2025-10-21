package org.ever._4ever_be_scm.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ever._4ever_be_scm.common.repository.TransactionChangeLogRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 오래된 트랜잭션 로그 정리를 담당하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogCleanupService {

    private final TransactionChangeLogRepository changeLogRepository;
    
    /**
     * 30일 이상 된 보상 완료된 로그 삭제
     * 매일 오전 1시에 실행
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void cleanupOldLogs() {
        // 30일 이상 된 로그 삭제
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        int deleted = changeLogRepository.deleteCompensatedBefore(threshold);
        log.info("Deleted {} old change logs", deleted);
    }
}
