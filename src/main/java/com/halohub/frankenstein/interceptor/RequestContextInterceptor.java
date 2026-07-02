package com.halohub.frankenstein.interceptor;

import com.halohub.frankenstein.common.constant.AuthConstants;
import com.halohub.frankenstein.common.context.RequestContextHelper;
import com.halohub.frankenstein.pojo.UserInfoContextBo;
import com.halohub.frankenstein.satoken.StpAdminUtil;
import com.halohub.frankenstein.satoken.StpMemberUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RequestContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String uri = request.getRequestURI();
        if (uri.startsWith("/admin/") && StpAdminUtil.isLogin()) {
            RequestContextHelper.setUser(UserInfoContextBo.builder()
                    .userId(StpAdminUtil.getLoginIdAsLong())
                    .loginType(AuthConstants.LOGIN_TYPE_ADMIN)
                    .build());
        } else if (uri.startsWith("/user/") && StpMemberUtil.isLogin()) {
            RequestContextHelper.setUser(UserInfoContextBo.builder()
                    .userId(StpMemberUtil.getLoginIdAsLong())
                    .loginType(AuthConstants.LOGIN_TYPE_MEMBER)
                    .build());
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        RequestContextHelper.clear();
    }
}
