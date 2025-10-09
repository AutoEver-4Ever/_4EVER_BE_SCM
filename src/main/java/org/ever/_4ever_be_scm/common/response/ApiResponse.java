package org.ever._4ever_be_scm.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final int status;
    private final boolean success;
    private final String message;
    private final T data;

    /**
     * 성공 응답
     */
    public static <T> ApiResponse<T> success(T data, String message, HttpStatus status) {
        return new ApiResponse<>(
            status.value(),
            true,
            message,
            data
        );
    }

    /**
     * 실패 응답
     */
    public static <T> ApiResponse<T> fail(String message, HttpStatus status) {
        return new ApiResponse<>(
            status.value(),
            false,
            message,
            null
        );
    }

    /**
     * 실패 응답 - 데이터 포함 (에러 상세 정보)
     */
    public static <T> ApiResponse<T> fail(String message, HttpStatus status, T data) {
        return new ApiResponse<>(
            status.value(),
            false,
            message,
            data
        );
    }
}
