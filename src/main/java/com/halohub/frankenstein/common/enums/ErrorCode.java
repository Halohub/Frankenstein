package com.halohub.frankenstein.common.enums;

import com.halohub.frankenstein.common.i18n.ErrorMessageSupport;

import java.util.Locale;

/**
 * Contract for error codes. Message text is resolved from i18n property files via {@link #getMessage(String)}.
 */
public interface ErrorCode {

    Integer getCode();

    /**
     * Key used in i18n property files (e.g. {@code error_messages.properties}).
     */
    String getMessageKey();

    /**
     * Resolves the localized message for the given language tag (e.g. {@code en}, {@code zh}, {@code ja}).
     */
    default String getMessage(String language, Object... args) {
        return ErrorMessageSupport.resolve(this, language, args);
    }

    /**
     * Resolves the localized message for the given locale.
     */
    default String getMessage(Locale locale, Object... args) {
        return ErrorMessageSupport.resolve(this, locale, args);
    }
}
