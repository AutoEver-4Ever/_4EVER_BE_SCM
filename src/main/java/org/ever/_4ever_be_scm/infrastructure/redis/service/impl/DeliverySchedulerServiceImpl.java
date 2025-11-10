package org.ever._4ever_be_scm.infrastructure.redis.service.impl;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.ever._4ever_be_scm.infrastructure.redis.dto.DeliveryCompletionTask;
import org.ever._4ever_be_scm.infrastructure.redis.service.DeliverySchedulerService;
import org.ever._4ever_be_scm.scm.mm.service.PurchaseOrderService;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 배송 완료 자동 스케줄링 서비스 구현
 * Redisson DelayedQueue를 사용한 지연 작업 처리
 */
@Slf4j
@Service
public class DeliverySchedulerServiceImpl implements DeliverySchedulerService {

    private final RedissonClient redissonClient;
    private final PurchaseOrderService purchaseOrderService;

    /**
     * 생성자 - PurchaseOrderService에 @Lazy 적용하여 순환 참조 방지
     */
    public DeliverySchedulerServiceImpl(
            RedissonClient redissonClient,
            @Lazy PurchaseOrderService purchaseOrderService) {
        this.redissonClient = redissonClient;
        this.purchaseOrderService = purchaseOrderService;
    }

    private RBlockingQueue<DeliveryCompletionTask> blockingQueue;
    private RDelayedQueue<DeliveryCompletionTask> delayedQueue;
    private ExecutorService executorService;

    /**
     * 애플리케이션 시작 시 DelayedQueue와 리스너 초기화
     */
    @PostConstruct
    public void init() {
        // BlockingQueue 생성 (실제 작업이 들어가는 큐)
        blockingQueue = redissonClient.getBlockingQueue("delivery_completion_queue");

        // DelayedQueue 생성 (지연 시간 관리)
        delayedQueue = redissonClient.getDelayedQueue(blockingQueue);

        // 리스너 스레드 시작
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(this::startListener);

        log.info("DeliverySchedulerService 초기화 완료 - DelayedQueue 리스너 시작");
    }

    /**
     * 배송 완료 작업 스케줄링
     *
     * @param purchaseOrderId 발주서 ID
     * @param delay 지연 시간 (배송 소요 기간)
     */
    @Override
    public void scheduleDeliveryCompletion(String purchaseOrderId, Duration delay) {
        LocalDateTime scheduledTime = LocalDateTime.now().plus(delay);

        DeliveryCompletionTask task = DeliveryCompletionTask.builder()
                .purchaseOrderId(purchaseOrderId)
                .scheduledCompletionTime(scheduledTime)
                .createdAt(LocalDateTime.now())
                .build();

        // DelayedQueue에 작업 추가 (지연 시간 후 자동으로 BlockingQueue로 이동)
        delayedQueue.offer(task, delay.toMillis(), TimeUnit.MILLISECONDS);

        log.info("배송 완료 작업 스케줄링 완료 - purchaseOrderId: {}, 지연시간: {}초, 예정시간: {}",
                purchaseOrderId, delay.getSeconds(), scheduledTime);
    }

    /**
     * 스케줄링된 작업 취소
     *
     * @param purchaseOrderId 발주서 ID
     * @return 취소 성공 여부
     */
    @Override
    public boolean cancelScheduledDelivery(String purchaseOrderId) {
        try {
            // DelayedQueue에서 해당 작업 제거
            boolean removed = delayedQueue.removeIf(task ->
                    task.getPurchaseOrderId().equals(purchaseOrderId));

            if (removed) {
                log.info("배송 완료 작업 취소 성공 - purchaseOrderId: {}", purchaseOrderId);
            } else {
                log.warn("배송 완료 작업 취소 실패 - 작업을 찾을 수 없음: {}", purchaseOrderId);
            }

            return removed;
        } catch (Exception e) {
            log.error("배송 완료 작업 취소 중 오류 발생 - purchaseOrderId: {}", purchaseOrderId, e);
            return false;
        }
    }

    /**
     * DelayedQueue 리스너
     * 지연 시간이 만료된 작업을 자동으로 수신하여 처리
     */
    private void startListener() {
        log.info("DeliveryCompletionTask 리스너 시작");

        while (!Thread.currentThread().isInterrupted()) {
            try {
                // 블로킹 방식으로 대기 (작업이 들어올 때까지 대기)
                DeliveryCompletionTask task = blockingQueue.take();

                log.info("배송 완료 작업 실행 시작 - purchaseOrderId: {}, 예정시간: {}",
                        task.getPurchaseOrderId(), task.getScheduledCompletionTime());

                // 실제 배송 완료 처리 실행
                executeDeliveryCompletion(task);

            } catch (InterruptedException e) {
                log.info("DeliveryCompletionTask 리스너 중단됨");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("배송 완료 작업 처리 중 오류 발생", e);
                // 오류 발생 시에도 리스너는 계속 동작
            }
        }

        log.info("DeliveryCompletionTask 리스너 종료");
    }

    /**
     * 배송 완료 처리 실행
     *
     * @param task 배송 완료 작업
     */
    private void executeDeliveryCompletion(DeliveryCompletionTask task) {
        try {
            String purchaseOrderId = task.getPurchaseOrderId();

            log.info("자동 입고 완료 처리 시작 - purchaseOrderId: {}", purchaseOrderId);

            // PurchaseOrderService의 completeDelivery 호출
            purchaseOrderService.completeDelivery(purchaseOrderId);

            log.info("자동 입고 완료 처리 성공 - purchaseOrderId: {}", purchaseOrderId);

        } catch (Exception e) {
            log.error("자동 입고 완료 처리 실패 - purchaseOrderId: {}, error: {}",
                    task.getPurchaseOrderId(), e.getMessage(), e);

            // 실패 시 재처리 로직이 필요하다면 여기에 추가
            // 예: 재시도 큐에 추가, 알림 발송 등
        }
    }

    /**
     * 애플리케이션 종료 시 리소스 정리
     */
    @PreDestroy
    public void destroy() {
        log.info("DeliverySchedulerService 종료 시작");

        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        log.info("DeliverySchedulerService 종료 완료");
    }
}
