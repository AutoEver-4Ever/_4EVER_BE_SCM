package org.ever._4ever_be_scm.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

// Request per 단위 Filter

@Component
@Slf4j
public class LoggingFilter extends OncePerRequestFilter {

    private static final long MAX_LOG_LENGTH = 1000;

    public String maskSensitiveData(String requestBody) {
        return requestBody.replaceAll("\"password\"\\s*:\\s*\"(.*?)\"", "\"password\":\"****\"");
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest req,
        HttpServletResponse res,
        FilterChain chain
    ) throws ServletException, IOException {

        if (isAsyncDispatch(req)) {
            chain.doFilter(req, res);
            return;
        }

        long start = System.currentTimeMillis();
        ContentCachingRequestWrapper wrappedReq = new ContentCachingRequestWrapper(req);
        ContentCachingResponseWrapper wrappedRes = new ContentCachingResponseWrapper(res);

        try {
            chain.doFilter(wrappedReq, wrappedRes);
        } finally {
            if (wrappedReq.isAsyncStarted()) {
                wrappedReq.getAsyncContext().addListener(
                    new AsyncRequestLoggingListener(this, wrappedReq, wrappedRes, start),
                        wrappedReq, wrappedRes
                );
            } else {
                String requestBody = new String(
                        wrappedReq.getContentAsByteArray(),
                        StandardCharsets.UTF_8
                ).trim();

                if (!requestBody.isEmpty()) {
                    log.info("[INFO] 요청 본문: {}", maskSensitiveData(requestBody));
                }

                byte[] responseByte = wrappedRes.getContentAsByteArray();
                String responseSummary = "";

                if (responseByte.length > 0) {
                    String responseBody = new String(responseByte, StandardCharsets.UTF_8).trim();
                    responseSummary = responseBody.length() > MAX_LOG_LENGTH
                            ? "응답 본문: 너무 커서 생략됨"
                            : "응답 본문: " + responseBody;
                }

                log.info("HTTP 메서드: [ {} ] 엔드포인트: [ {} ] Content-Type: [ {} ] " +
                    "Authorization: [ {} ] User-agent: [ {} ] Host: [ {} ] " +
                    "Content-length: [ {} ] 응답 본문: [ {} ]",
                    req.getMethod(), req.getRequestURI(),
                    req.getHeader("content-type"),
                    req.getHeader("authorization"),
                    req.getHeader("member-agent"),
                    req.getHeader("host"),
                    req.getHeader("content-length"),
                    responseSummary);


                long end = System.currentTimeMillis();
                log.info(">>> 소요 시간: {} sec", (end - start) / 1000.0);

                wrappedRes.copyBodyToResponse();   // 반드시 finally에서 호출
            }
        }
    }
}
