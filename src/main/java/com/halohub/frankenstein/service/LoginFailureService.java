package com.halohub.frankenstein.service;

import com.halohub.frankenstein.common.properties.AuthProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class LoginFailureService {

    private static final String KEY_PREFIX = "auth:login:fail:";

    private final StringRedisTemplate redisTemplate;
    private final AuthProperties authProperties;

    public LoginFailureService(StringRedisTemplate redisTemplate, AuthProperties authProperties) {
        this.redisTemplate = redisTemplate;
        this.authProperties = authProperties;
    }

    public boolean isLocked(String loginKey) {
        String count = redisTemplate.opsForValue().get(KEY_PREFIX + loginKey);
        return count != null && Integer.parseInt(count) >= authProperties.getLoginMaxFailures();
    }

    public void recordFailure(String loginKey) {
        String key = KEY_PREFIX + loginKey;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, authProperties.getLoginLockMinutes(), TimeUnit.MINUTES);
        }
    }

    public void clearFailures(String loginKey) {
        redisTemplate.delete(KEY_PREFIX + loginKey);
    }

    public long getRemainingLockMinutes(String loginKey) {
        Long ttl = redisTemplate.getExpire(KEY_PREFIX + loginKey, TimeUnit.MINUTES);
        return ttl != null && ttl > 0 ? ttl : authProperties.getLoginLockMinutes();
    }
}
