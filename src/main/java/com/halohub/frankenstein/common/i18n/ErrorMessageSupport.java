package com.halohub.frankenstein.common.i18n;

import com.halohub.frankenstein.common.enums.ErrorCode;

import java.util.Locale;

/**
 * Static bridge so {@link ErrorCode#getMessage(String)} can resolve messages without injecting the resolver.
 */
public final class ErrorMessageSupport {

    private static volatile ErrorMessageResolver resolver;

    private ErrorMessageSupport() {
    }

    static void register(ErrorMessageResolver errorMessageResolver) {
        resolver = errorMessageResolver;
    }

    public static String resolve(ErrorCode errorCode, String language, Object... args) {
        if (resolver != null) {
            return resolver.resolve(errorCode, language, args);
        }
        return errorCode.getMessageKey();
    }

    public static String resolve(ErrorCode errorCode, Locale locale, Object... args) {
        if (resolver != null) {
            return resolver.resolve(errorCode, locale, args);
        }
        return errorCode.getMessageKey();
    }
}
