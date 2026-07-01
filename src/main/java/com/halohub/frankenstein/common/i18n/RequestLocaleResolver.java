package com.halohub.frankenstein.common.i18n;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * Resolves request locale from {@code X-Lang} / {@code lang} header or query param, then {@code Accept-Language}.
 */
public final class RequestLocaleResolver {

    public static final String LANG_HEADER = "X-Lang";
    public static final String LANG_PARAM = "lang";

    private RequestLocaleResolver() {
    }

    public static Locale resolve(HttpServletRequest request) {
        if (request == null) {
            return Locale.ENGLISH;
        }
        String lang = request.getHeader(LANG_HEADER);
        if (!StringUtils.hasText(lang)) {
            lang = request.getParameter(LANG_PARAM);
        }
        if (StringUtils.hasText(lang)) {
            return LocaleSupport.parse(lang);
        }
        Locale acceptLanguage = request.getLocale();
        return acceptLanguage != null ? acceptLanguage : Locale.ENGLISH;
    }
}
