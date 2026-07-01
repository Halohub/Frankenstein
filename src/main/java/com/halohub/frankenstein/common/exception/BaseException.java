package com.halohub.frankenstein.common.exception;

import com.halohub.frankenstein.common.enums.CommonErrorCode;
import com.halohub.frankenstein.common.enums.ErrorCode;
import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Integer code;
    private final String message;
    private final Object[] messageArgs;

    public BaseException() {
        this(CommonErrorCode.UNKNOWN_ERROR);
    }

    public BaseException(String msg) {
        super(msg);
        this.errorCode = CommonErrorCode.UNKNOWN_ERROR;
        this.code = CommonErrorCode.UNKNOWN_ERROR.getCode();
        this.message = msg;
        this.messageArgs = null;
    }

    public BaseException(ErrorCode errorCode) {
        this(errorCode, (Object[]) null);
    }

    public BaseException(ErrorCode errorCode, Object... messageArgs) {
        super(errorCode.getMessageKey());
        this.errorCode = errorCode;
        this.code = errorCode.getCode();
        this.message = null;
        this.messageArgs = messageArgs;
    }

    public BaseException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
        this.code = errorCode.getCode();
        this.message = customMessage;
        this.messageArgs = null;
    }

    @Override
    public String getMessage() {
        return message != null ? message : super.getMessage();
    }
}
