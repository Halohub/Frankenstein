package com.halohub.frankenstein.common.result;

import com.halohub.frankenstein.common.enums.ErrorCode;
import com.halohub.frankenstein.common.i18n.ErrorMessageSupport;
import lombok.Data;

import java.io.Serializable;
import java.util.Locale;

/**
 * Unified API response envelope: flag=1 success, 0 business failure, 2 system exception.
 */
@Data
public class Result<T> implements Serializable {

    private Integer flag;
    private String desc;
    private T data;

    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.flag = 1;
        return result;
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.data = data;
        result.flag = 1;
        return result;
    }

    public static <T> Result<T> success(String desc) {
        Result<T> result = new Result<>();
        result.desc = desc;
        result.flag = 1;
        return result;
    }

    public static <T> Result<T> success(T data, String desc) {
        Result<T> result = new Result<>();
        result.data = data;
        result.flag = 1;
        result.desc = desc;
        return result;
    }

    public static <T> Result<T> error(String desc) {
        Result<T> result = new Result<>();
        result.desc = desc;
        result.flag = 0;
        return result;
    }

    public static <T> Result<T> error(Integer code, String desc) {
        Result<T> result = new Result<>();
        result.desc = desc;
        result.flag = code;
        return result;
    }

    public static <T> Result<T> error(ErrorCode errorCode) {
        return error(errorCode, "en");
    }

    public static <T> Result<T> error(ErrorCode errorCode, String language, Object... args) {
        Result<T> result = new Result<>();
        result.desc = ErrorMessageSupport.resolve(errorCode, language, args);
        result.flag = errorCode.getCode();
        return result;
    }

    public static <T> Result<T> error(ErrorCode errorCode, Locale locale, Object... args) {
        Result<T> result = new Result<>();
        result.desc = ErrorMessageSupport.resolve(errorCode, locale, args);
        result.flag = errorCode.getCode();
        return result;
    }
}
