package org.ever._4ever_be_scm.common.exception;

/**
 * 데이터베이스 관련 예외
 */
public class DatabaseException extends BusinessException {

    public DatabaseException(ErrorCode errorCode) {
        super(errorCode);
    }

    public DatabaseException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }

    public DatabaseException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public DatabaseException(ErrorCode errorCode, String detail, Throwable cause) {
        super(errorCode, detail, cause);
    }
}
