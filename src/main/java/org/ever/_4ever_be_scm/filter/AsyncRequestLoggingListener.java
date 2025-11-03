package org.ever._4ever_be_scm.filter;

import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

@Slf4j
class AsyncRequestLoggingListener implements AsyncListener {

    private final LoggingFilter loggingFilter;
    private final ContentCachingRequestWrapper requestWrapper;
    private final ContentCachingResponseWrapper responseWrapper;
    private final long startTime;

    public AsyncRequestLoggingListener(
            LoggingFilter loggingFilter,
            ContentCachingRequestWrapper requestWrapper,
            ContentCachingResponseWrapper responseWrapper,
            long startTime
    ) {
        this.loggingFilter = loggingFilter;
        this.requestWrapper = requestWrapper;
        this.responseWrapper = responseWrapper;
        this.startTime = startTime;
    }

    @Override
    public void onComplete(AsyncEvent event) throws IOException {
        log.info("[INFO] 완료, request: {}, response: {}", requestWrapper.getRequestURI(), responseWrapper.getStatus());
        responseWrapper.copyBodyToResponse();
    }

    @Override public void onTimeout(AsyncEvent event) {
        log.warn("[WARN] Async 요청 타임아웃: URI: {}", requestWrapper.getRequestURI());
    }

    @Override public void onError(AsyncEvent event) {
        log.error("[ERROR] Async 요청 처리 중 오류: URI: {}, error: {}", requestWrapper.getRequestURI(), event.getThrowable());
    }

    @Override
    public void onStartAsync(AsyncEvent event) {
        event.getAsyncContext().addListener(
            new AsyncRequestLoggingListener(
                    loggingFilter,
                    requestWrapper,
                    responseWrapper,
                    startTime
            ),
                requestWrapper,
                responseWrapper
        );
    }
}