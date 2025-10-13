package org.ever._4ever_be_scm.common.exception;

/**
 * SCM 관련 예외
 */
public class ScmException extends BusinessException {

    public ScmException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ScmException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }

    public ScmException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public ScmException(ErrorCode errorCode, String detail, Throwable cause) {
        super(errorCode, detail, cause);
    }
}
