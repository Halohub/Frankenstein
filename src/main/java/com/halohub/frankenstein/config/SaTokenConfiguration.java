package com.halohub.frankenstein.config;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import com.halohub.frankenstein.interceptor.RequestContextInterceptor;
import com.halohub.frankenstein.satoken.StpAdminUtil;
import com.halohub.frankenstein.satoken.StpMemberUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Slf4j
public class SaTokenConfiguration implements WebMvcConfigurer {

    private final RequestContextInterceptor requestContextInterceptor;

    public SaTokenConfiguration(RequestContextInterceptor requestContextInterceptor) {
        this.requestContextInterceptor = requestContextInterceptor;
    }

    @PostConstruct
    public void registerStpLogic() {
        SaTokenConfig global = SaManager.getConfig();

        SaTokenConfig adminConfig = copyConfig(global);
        adminConfig.setMaxLoginCount(1);
        adminConfig.setIsConcurrent(false);
        StpAdminUtil.getStpLogic().setConfig(adminConfig);

        SaTokenConfig memberConfig = copyConfig(global);
        memberConfig.setMaxLoginCount(global.getMaxLoginCount());
        memberConfig.setIsConcurrent(true);
        StpMemberUtil.getStpLogic().setConfig(memberConfig);

        SaManager.putStpLogic(StpAdminUtil.getStpLogic());
        SaManager.putStpLogic(StpMemberUtil.getStpLogic());
        log.info("Sa-Token StpLogic registered: admin={}, member={}",
                StpAdminUtil.TYPE, StpMemberUtil.TYPE);
    }

    private SaTokenConfig copyConfig(SaTokenConfig source) {
        SaTokenConfig target = new SaTokenConfig();
        target.setTokenName(source.getTokenName());
        target.setTimeout(source.getTimeout());
        target.setActiveTimeout(source.getActiveTimeout());
        target.setIsConcurrent(source.getIsConcurrent());
        target.setIsShare(source.getIsShare());
        target.setMaxLoginCount(source.getMaxLoginCount());
        target.setTokenStyle(source.getTokenStyle());
        target.setIsLog(source.getIsLog());
        target.setIsReadHeader(source.getIsReadHeader());
        target.setIsReadCookie(source.getIsReadCookie());
        target.setIsReadBody(source.getIsReadBody());
        target.setTokenPrefix(source.getTokenPrefix());
        return target;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            SaRouter.match("/admin/api_v1/**")
                    .notMatch(
                            "/admin/api_v1/auth/login",
                            "/public/**"
                    )
                    .check(r -> StpAdminUtil.checkLogin());

            SaRouter.match("/user/api_v1/**")
                    .notMatch(
                            "/user/api_v1/auth/login",
                            "/user/api_v1/auth/register",
                            "/public/**"
                    )
                    .check(r -> StpMemberUtil.checkLogin());
        })).addPathPatterns("/**");

        registry.addInterceptor(requestContextInterceptor).addPathPatterns("/**");
    }
}
