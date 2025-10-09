package org.ever._4ever_be_scm.infrastructure.redis.service;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 서비스 인터페이스
 */
public interface RedisService {

    /**
     * 데이터 저장
     */
    void set(String key, Object value);

    /**
     * 데이터 저장 (만료 시간 포함)
     */
    void set(String key, Object value, long timeout, TimeUnit unit);

    /**
     * 데이터 저장 (Duration 사용)
     */
    void set(String key, Object value, Duration duration);

    /**
     * 데이터 조회
     */
    Object get(String key);

    /**
     * 데이터 삭제
     */
    Boolean delete(String key);

    /**
     * 여러 데이터 삭제
     */
    Long delete(Set<String> keys);

    /**
     * 데이터 존재 여부 확인
     */
    Boolean hasKey(String key);

    /**
     * 만료 시간 설정
     */
    Boolean expire(String key, long timeout, TimeUnit unit);

    /**
     * 만료 시간 조회 (초 단위)
     */
    Long getExpire(String key);

    /**
     * 증가 (Increment)
     */
    Long increment(String key);

    /**
     * 증가 (Increment) - 특정 값만큼
     */
    Long increment(String key, long delta);

    /**
     * 감소 (Decrement)
     */
    Long decrement(String key);

    /**
     * 감소 (Decrement) - 특정 값만큼
     */
    Long decrement(String key, long delta);

    /**
     * Hash 데이터 저장
     */
    void hSet(String key, String hashKey, Object value);

    /**
     * Hash 데이터 조회
     */
    Object hGet(String key, String hashKey);

    /**
     * Hash 데이터 삭제
     */
    Long hDelete(String key, Object... hashKeys);

    /**
     * Hash 데이터 존재 여부
     */
    Boolean hHasKey(String key, String hashKey);

    /**
     * Set 데이터 추가
     */
    Long sAdd(String key, Object... values);

    /**
     * Set 데이터 조회
     */
    Set<Object> sMembers(String key);

    /**
     * Set 데이터 삭제
     */
    Long sRemove(String key, Object... values);

    /**
     * Set 데이터 존재 여부
     */
    Boolean sIsMember(String key, Object value);
}
