package org.ever._4ever_be_scm.common.saga;

import lombok.experimental.UtilityClass;

/**
 * Saga 트랜잭션 컨텍스트 정보를 관리하는 유틸리티 클래스
 * ThreadLocal을 통해 현재 실행 중인 트랜잭션 ID를 관리
 */
@UtilityClass
public class SagaTransactionContext {
    private static final ThreadLocal<String> currentTransactionId = new ThreadLocal<>();

    /**
     * 현재 쓰레드의 트랜잭션 ID를 반환
     * @return 현재 트랜잭션 ID, 없으면 null
     */
    public static String getCurrentTransactionId() {
        return currentTransactionId.get();
    }

    /**
     * 현재 쓰레드에 트랜잭션 ID 설정
     * @param transactionId 설정할 트랜잭션 ID
     */
    public static void setCurrentTransactionId(String transactionId) {
        currentTransactionId.set(transactionId);
    }

    /**
     * 현재 쓰레드의 트랜잭션 컨텍스트 정리
     */
    public static void clear() {
        currentTransactionId.remove();
    }
}
