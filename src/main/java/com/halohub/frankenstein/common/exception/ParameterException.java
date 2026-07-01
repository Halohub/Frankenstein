package com.halohub.frankenstein.common.exception;

import com.halohub.frankenstein.common.enums.CommonErrorCode;
import com.halohub.frankenstein.common.enums.ErrorCode;

public class ParameterException extends BaseException {

    public ParameterException() {
        super(CommonErrorCode.PARAM_EXCEPTION);
    }

    public ParameterException(String msg) {
        super(msg);
    }

    public ParameterException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ParameterException(ErrorCode errorCode, String customMessage) {
        super(errorCode, customMessage);
    }
}
