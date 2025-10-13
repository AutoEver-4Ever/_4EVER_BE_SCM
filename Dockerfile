# Production Dockerfile
FROM gradle:7.6.1-jdk17 AS build
WORKDIR /app

# Gradle 래퍼와 빌드 파일들을 먼저 복사하여 캐싱 활용
COPY gradle gradle
COPY gradlew .
COPY build.gradle .
COPY settings.gradle .

# 의존성 다운로드 (캐싱을 위해 별도 실행)
RUN chmod +x ./gradlew
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사
COPY src src

# 테스트 포함하여 빌드 (Production에서는 테스트 필수)
RUN ./gradlew clean build --no-daemon

FROM openjdk:17-slim
WORKDIR /app

# 보안 및 성능을 위한 non-root 유저 생성
RUN groupadd -r appuser && useradd -r -g appuser appuser

# curl 설치 (healthcheck용)
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*

# JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 파일 소유권 변경
RUN chown appuser:appuser app.jar

# non-root 유저로 전환
USER appuser

EXPOSE 8080

# Production 환경용 JVM 옵션 (메모리 최적화, GC 튜닝)
ENV JAVA_OPTS="-Xms512m -Xmx1024m \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -XX:+UseStringDeduplication \
    -Dspring.profiles.active=prod \
    -Djava.security.egd=file:/dev/./urandom"

# Healthcheck 설정
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
