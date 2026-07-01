package com.halohub.frankenstein.common.i18n;

import org.springframework.util.StringUtils;

import java.util.Locale;

public final class LocaleSupport {

    private LocaleSupport() {
    }

    /**
     * Parses a language tag such as {@code en}, {@code zh}, {@code zh-CN}, or {@code ja}.
     */
    public static Locale parse(String language) {
        if (!StringUtils.hasText(language)) {
            return Locale.ENGLISH;
        }
        String normalized = language.trim().replace('_', '-');
        if ("zh".equalsIgnoreCase(normalized) || normalized.toLowerCase(Locale.ROOT).startsWith("zh-")) {
            return Locale.SIMPLIFIED_CHINESE;
        }
        if ("ja".equalsIgnoreCase(normalized) || normalized.toLowerCase(Locale.ROOT).startsWith("ja-")) {
            return Locale.JAPANESE;
        }
        if ("en".equalsIgnoreCase(normalized) || normalized.toLowerCase(Locale.ROOT).startsWith("en-")) {
            return Locale.ENGLISH;
        }
        return Locale.forLanguageTag(normalized);
    }
}
