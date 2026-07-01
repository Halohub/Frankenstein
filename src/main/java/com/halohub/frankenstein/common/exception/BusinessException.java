package com.halohub.frankenstein.common.exception;

import com.halohub.frankenstein.common.enums.CommonErrorCode;
import com.halohub.frankenstein.common.enums.ErrorCode;

public class BusinessException extends BaseException {

    public BusinessException() {
        super(CommonErrorCode.BUSINESS_EXCEPTION);
    }

    public BusinessException(String msg) {
        super(msg);
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(errorCode, customMessage);
    }
}
