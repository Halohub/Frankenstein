package com.halohub.frankenstein.common.exception;

import com.halohub.frankenstein.common.enums.CommonErrorCode;
import com.halohub.frankenstein.common.enums.ErrorCode;

public class RateLimitException extends BaseException {

    public RateLimitException() {
        super(CommonErrorCode.RATE_LIMIT_EXCEEDED);
    }

    public RateLimitException(String msg) {
        super(msg);
    }

    public RateLimitException(ErrorCode errorCode) {
        super(errorCode);
    }

    public RateLimitException(ErrorCode errorCode, String customMessage) {
        super(errorCode, customMessage);
    }
}
