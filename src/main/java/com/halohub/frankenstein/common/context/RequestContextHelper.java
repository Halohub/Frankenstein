package com.halohub.frankenstein.common.context;

import com.halohub.frankenstein.pojo.UserInfoContextBo;

public final class RequestContextHelper {

    private RequestContextHelper() {
    }

    public static void setUser(UserInfoContextBo user) {
        ThreadLocalContext.set(user);
    }

    public static UserInfoContextBo getUser() {
        return ThreadLocalContext.get();
    }

    public static Long getCurrentUserId() {
        UserInfoContextBo user = getUser();
        return user != null ? user.getUserId() : null;
    }

    public static void clear() {
        ThreadLocalContext.clear();
    }
}
