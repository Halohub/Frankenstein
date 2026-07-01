package com.halohub.frankenstein.common.i18n;

import com.halohub.frankenstein.common.enums.ErrorCode;
import com.halohub.frankenstein.common.result.Result;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Resolves localized error messages from {@code i18n/error_messages*.properties}.
 */
@Component
public class ErrorMessageResolver {

    private final MessageSource errorMessageSource;

    public ErrorMessageResolver(@Qualifier("errorMessageSource") MessageSource errorMessageSource) {
        this.errorMessageSource = errorMessageSource;
        ErrorMessageSupport.register(this);
    }

    public String resolve(ErrorCode errorCode, String language, Object... args) {
        return resolve(errorCode, LocaleSupport.parse(language), args);
    }

    public String resolve(ErrorCode errorCode, Locale locale, Object... args) {
        Locale target = locale != null ? locale : Locale.ENGLISH;
        return errorMessageSource.getMessage(errorCode.getMessageKey(), args, errorCode.getMessageKey(), target);
    }

    public String resolve(ErrorCode errorCode, Object... args) {
        return resolve(errorCode, LocaleContextHolder.getLocale(), args);
    }

    public <T> Result<T> toErrorResult(ErrorCode errorCode, String language, Object... args) {
        return Result.error(errorCode.getCode(), resolve(errorCode, language, args));
    }

    public <T> Result<T> toErrorResult(ErrorCode errorCode, Locale locale, Object... args) {
        return Result.error(errorCode.getCode(), resolve(errorCode, locale, args));
    }

    public <T> Result<T> toErrorResult(ErrorCode errorCode, Object... args) {
        return Result.error(errorCode.getCode(), resolve(errorCode, args));
    }
}
