package com.halohub.frankenstein.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.halohub.frankenstein.common.constant.AuthConstants;
import com.halohub.frankenstein.common.enums.CommonErrorCode;
import com.halohub.frankenstein.common.exception.BusinessException;
import com.halohub.frankenstein.dto.LoginRequest;
import com.halohub.frankenstein.entity.SysAdmin;
import com.halohub.frankenstein.mapper.SysAdminMapper;
import com.halohub.frankenstein.satoken.StpAdminUtil;
import com.halohub.frankenstein.vo.AuthInfoVO;
import com.halohub.frankenstein.vo.LoginVO;
import com.halohub.frankenstein.vo.RouteMenuVO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class AdminAuthService {

    private final SysAdminMapper sysAdminMapper;
    private final PasswordEncoder passwordEncoder;
    private final LoginFailureService loginFailureService;
    private final AdminMenuService adminMenuService;

    public AdminAuthService(SysAdminMapper sysAdminMapper,
                            PasswordEncoder passwordEncoder,
                            LoginFailureService loginFailureService,
                            AdminMenuService adminMenuService) {
        this.sysAdminMapper = sysAdminMapper;
        this.passwordEncoder = passwordEncoder;
        this.loginFailureService = loginFailureService;
        this.adminMenuService = adminMenuService;
    }

    public LoginVO login(LoginRequest request) {
        String lockKey = "admin:" + request.getUsername();
        if (loginFailureService.isLocked(lockKey)) {
            throw new BusinessException(CommonErrorCode.ACCOUNT_LOCKED,
                    loginFailureService.getRemainingLockMinutes(lockKey));
        }

        SysAdmin admin = sysAdminMapper.selectOne(new LambdaQueryWrapper<SysAdmin>()
                .eq(SysAdmin::getUsername, request.getUsername())
                .last("LIMIT 1"));
        if (admin == null) {
            loginFailureService.recordFailure(lockKey);
            throw new BusinessException(CommonErrorCode.ACCOUNT_NOT_FOUND);
        }
        if (admin.getStatus() == null || admin.getStatus() != AuthConstants.STATUS_ACTIVE) {
            throw new BusinessException(CommonErrorCode.STATUS_ERROR);
        }
        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            loginFailureService.recordFailure(lockKey);
            throw new BusinessException(CommonErrorCode.PASSWORD_ERROR);
        }

        loginFailureService.clearFailures(lockKey);

        StpAdminUtil.login(admin.getId());

        admin.setLastLoginTime(LocalDateTime.now());
        sysAdminMapper.updateById(admin);

        return LoginVO.builder()
                .id(admin.getId())
                .username(admin.getUsername())
                .token(StpAdminUtil.getTokenValue())
                .tokenType("Bearer")
                .build();
    }

    public void logout() {
        if (StpAdminUtil.isLogin()) {
            StpAdminUtil.logout();
        }
    }

    public AuthInfoVO currentUser() {
        long adminId = StpAdminUtil.getLoginIdAsLong();
        SysAdmin admin = sysAdminMapper.selectById(adminId);
        if (admin == null) {
            throw new BusinessException(CommonErrorCode.ACCOUNT_NOT_FOUND);
        }
        List<String> roles = sysAdminMapper.listRoleCodesByAdminId(adminId);
        List<String> permissions = adminMenuService.isSuperAdmin(adminId)
                ? adminMenuService.listAllAdminPermCodes()
                : sysAdminMapper.listPermCodesByAdminId(adminId);
        return AuthInfoVO.builder()
                .id(admin.getId())
                .username(admin.getUsername())
                .nickname(admin.getNickname())
                .roles(roles)
                .permissions(permissions)
                .build();
    }

    public List<RouteMenuVO> currentMenus(Locale locale) {
        long adminId = StpAdminUtil.getLoginIdAsLong();
        return adminMenuService.listMenus(adminId, locale);
    }
}
