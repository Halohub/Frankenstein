package com.halohub.frankenstein.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.halohub.frankenstein.common.constant.PermissionConstants;
import com.halohub.frankenstein.common.i18n.LocaleSupport;
import com.halohub.frankenstein.entity.SysI18nMessage;
import com.halohub.frankenstein.mapper.SysI18nMessageMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class I18nMessageService {

    private final SysI18nMessageMapper sysI18nMessageMapper;

    public I18nMessageService(SysI18nMessageMapper sysI18nMessageMapper) {
        this.sysI18nMessageMapper = sysI18nMessageMapper;
    }

    public String resolvePermissionName(Long permissionId, String defaultName, Locale locale) {
        Map<Long, String> names = batchResolvePermissionNames(List.of(permissionId), locale);
        return names.getOrDefault(permissionId, defaultName);
    }

    public Map<Long, String> batchResolvePermissionNames(Collection<Long> permissionIds, Locale locale) {
        if (CollectionUtils.isEmpty(permissionIds)) {
            return Map.of();
        }
        String lang = toLangKey(locale);
        List<SysI18nMessage> messages = sysI18nMessageMapper.selectList(new LambdaQueryWrapper<SysI18nMessage>()
                .eq(SysI18nMessage::getRefType, PermissionConstants.I18N_REF_PERMISSION)
                .in(SysI18nMessage::getRefId, permissionIds)
                .eq(SysI18nMessage::getLocale, lang)
                .eq(SysI18nMessage::getFieldName, PermissionConstants.I18N_FIELD_PERM_NAME));
        Map<Long, String> result = messages.stream()
                .collect(Collectors.toMap(SysI18nMessage::getRefId, SysI18nMessage::getFieldValue, (a, b) -> a));
        if (Locale.ENGLISH.equals(locale) || "en".equals(lang)) {
            return result;
        }
        Collection<Long> missingIds = permissionIds.stream()
                .filter(id -> !result.containsKey(id))
                .collect(Collectors.toSet());
        if (missingIds.isEmpty()) {
            return result;
        }
        List<SysI18nMessage> fallbackMessages = sysI18nMessageMapper.selectList(new LambdaQueryWrapper<SysI18nMessage>()
                .eq(SysI18nMessage::getRefType, PermissionConstants.I18N_REF_PERMISSION)
                .in(SysI18nMessage::getRefId, missingIds)
                .eq(SysI18nMessage::getLocale, "en")
                .eq(SysI18nMessage::getFieldName, PermissionConstants.I18N_FIELD_PERM_NAME));
        Map<Long, String> merged = new HashMap<>(result);
        fallbackMessages.forEach(message -> merged.putIfAbsent(message.getRefId(), message.getFieldValue()));
        return merged;
    }

    private String toLangKey(Locale locale) {
        Locale normalized = locale != null ? locale : Locale.ENGLISH;
        if (Locale.SIMPLIFIED_CHINESE.equals(normalized) || "zh".equalsIgnoreCase(normalized.getLanguage())) {
            return "zh";
        }
        if (Locale.JAPANESE.equals(normalized) || "ja".equalsIgnoreCase(normalized.getLanguage())) {
            return "ja";
        }
        return "en";
    }

    public Locale normalize(Locale locale) {
        return locale != null ? LocaleSupport.parse(locale.toLanguageTag()) : Locale.ENGLISH;
    }
}
