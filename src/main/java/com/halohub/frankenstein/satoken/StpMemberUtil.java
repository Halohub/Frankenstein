package com.halohub.frankenstein.satoken;

import cn.dev33.satoken.stp.StpLogic;
import com.halohub.frankenstein.common.constant.AuthConstants;

public final class StpMemberUtil {

    public static final String TYPE = AuthConstants.LOGIN_TYPE_MEMBER;

    private static final StpLogic STP_LOGIC = new StpLogic(TYPE);

    private StpMemberUtil() {
    }

    public static StpLogic getStpLogic() {
        return STP_LOGIC;
    }

    public static void login(Object loginId) {
        STP_LOGIC.login(loginId);
    }

    public static void login(Object loginId, cn.dev33.satoken.stp.SaLoginModel loginModel) {
        STP_LOGIC.login(loginId, loginModel);
    }

    public static void logout() {
        STP_LOGIC.logout();
    }

    public static void logout(Object loginId) {
        STP_LOGIC.logout(loginId);
    }

    public static boolean isLogin() {
        return STP_LOGIC.isLogin();
    }

    public static void checkLogin() {
        STP_LOGIC.checkLogin();
    }

    public static Object getLoginId() {
        return STP_LOGIC.getLoginId();
    }

    public static long getLoginIdAsLong() {
        return STP_LOGIC.getLoginIdAsLong();
    }

    public static String getTokenValue() {
        return STP_LOGIC.getTokenValue();
    }

    public static void checkRole(String role) {
        STP_LOGIC.checkRole(role);
    }

    public static void checkPermission(String permission) {
        STP_LOGIC.checkPermission(permission);
    }
}
