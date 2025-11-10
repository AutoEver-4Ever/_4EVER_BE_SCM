package org.ever._4ever_be_scm.infrastructure.redis.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 설정
 * 지연 작업 처리를 위한 RedissonClient 구성
 */
@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    /**
     * RedissonClient 빈 생성
     * Lettuce와 독립적으로 동작하며 동일한 Redis 서버 사용
     */
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();

        // Single Server 모드 설정
        String redisAddress = String.format("redis://%s:%d", redisHost, redisPort);

        config.useSingleServer()
                .setAddress(redisAddress)
                .setPassword(redisPassword.isEmpty() ? null : redisPassword)
                .setConnectionPoolSize(10)
                .setConnectionMinimumIdleSize(2)
                .setRetryAttempts(3)
                .setRetryInterval(1500)
                .setTimeout(3000);

        // JSON 직렬화 설정 - LocalDateTime 지원을 위한 ObjectMapper 구성
        config.setCodec(new JsonJacksonCodec(createObjectMapper()));

        return Redisson.create(config);
    }

    /**
     * Redisson용 ObjectMapper 생성
     * LocalDateTime 등 Java 8 Time API 지원
     */
    private ObjectMapper createObjectMapper() {
        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(Object.class)
                .build();

        ObjectMapper mapper = new ObjectMapper();

        // Java 8 Time API (LocalDateTime 등) 지원
        mapper.registerModule(new JavaTimeModule());

        // 날짜를 ISO-8601 문자열로 직렬화 (타임스탬프 대신)
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 타입 정보 포함 (역직렬화 시 타입 복원)
        mapper.activateDefaultTyping(typeValidator, ObjectMapper.DefaultTyping.NON_FINAL);

        return mapper;
    }
}
