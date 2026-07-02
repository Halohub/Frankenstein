package com.halohub.frankenstein.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.halohub.frankenstein.common.constant.AuthConstants;
import com.halohub.frankenstein.common.enums.CommonErrorCode;
import com.halohub.frankenstein.common.exception.BusinessException;
import com.halohub.frankenstein.common.properties.AuthProperties;
import com.halohub.frankenstein.dto.LoginRequest;
import com.halohub.frankenstein.dto.MemberRegisterRequest;
import com.halohub.frankenstein.entity.SysMember;
import com.halohub.frankenstein.mapper.SysMemberMapper;
import com.halohub.frankenstein.satoken.StpMemberUtil;
import com.halohub.frankenstein.vo.AuthInfoVO;
import com.halohub.frankenstein.vo.LoginVO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MemberAuthService {

    private static final String DEFAULT_ROLE = "MEMBER_NORMAL";

    private final SysMemberMapper sysMemberMapper;
    private final PasswordEncoder passwordEncoder;
    private final LoginFailureService loginFailureService;
    private final AuthProperties authProperties;
    private final MemberSessionService memberSessionService;

    public MemberAuthService(SysMemberMapper sysMemberMapper,
                             PasswordEncoder passwordEncoder,
                             LoginFailureService loginFailureService,
                             AuthProperties authProperties,
                             MemberSessionService memberSessionService) {
        this.sysMemberMapper = sysMemberMapper;
        this.passwordEncoder = passwordEncoder;
        this.loginFailureService = loginFailureService;
        this.authProperties = authProperties;
        this.memberSessionService = memberSessionService;
    }

    @Transactional(rollbackFor = Exception.class)
    public LoginVO register(MemberRegisterRequest request) {
        ensureUsernameAvailable(request.getUsername());
        if (StringUtils.hasText(request.getPhone())) {
            ensurePhoneAvailable(request.getPhone());
        }
        if (StringUtils.hasText(request.getEmail())) {
            ensureEmailAvailable(request.getEmail());
        }

        SysMember member = new SysMember();
        member.setUsername(request.getUsername());
        member.setPassword(passwordEncoder.encode(request.getPassword()));
        member.setNickname(StringUtils.hasText(request.getNickname()) ? request.getNickname() : request.getUsername());
        member.setPhone(blankToNull(request.getPhone()));
        member.setEmail(blankToNull(request.getEmail()));
        member.setVipLevel(AuthConstants.VIP_LEVEL_NORMAL);
        member.setStatus(AuthConstants.STATUS_ACTIVE);
        member.setRegisterSource("web");
        sysMemberMapper.insert(member);

        sysMemberMapper.bindDefaultRole(member.getId(), DEFAULT_ROLE);
        return loginAfterRegister(member);
    }

    public LoginVO login(LoginRequest request) {
        String lockKey = "member:" + request.getUsername();
        if (loginFailureService.isLocked(lockKey)) {
            throw new BusinessException(CommonErrorCode.ACCOUNT_LOCKED,
                    loginFailureService.getRemainingLockMinutes(lockKey));
        }

        SysMember member = sysMemberMapper.selectOne(new LambdaQueryWrapper<SysMember>()
                .eq(SysMember::getUsername, request.getUsername())
                .last("LIMIT 1"));
        if (member == null) {
            loginFailureService.recordFailure(lockKey);
            throw new BusinessException(CommonErrorCode.ACCOUNT_NOT_FOUND);
        }
        if (member.getStatus() == null || member.getStatus() != AuthConstants.STATUS_ACTIVE) {
            throw new BusinessException(CommonErrorCode.STATUS_ERROR);
        }
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            loginFailureService.recordFailure(lockKey);
            throw new BusinessException(CommonErrorCode.PASSWORD_ERROR);
        }

        loginFailureService.clearFailures(lockKey);
        return doLogin(member);
    }

    public void logout() {
        if (StpMemberUtil.isLogin()) {
            StpMemberUtil.logout();
        }
    }

    public AuthInfoVO currentUser() {
        long memberId = StpMemberUtil.getLoginIdAsLong();
        SysMember member = sysMemberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException(CommonErrorCode.ACCOUNT_NOT_FOUND);
        }
        List<String> roles = sysMemberMapper.listRoleCodesByMemberId(memberId);
        List<String> permissions = sysMemberMapper.listPermCodesByMemberId(memberId);
        return AuthInfoVO.builder()
                .id(member.getId())
                .username(member.getUsername())
                .nickname(member.getNickname())
                .vipLevel(member.getVipLevel())
                .roles(roles)
                .permissions(permissions)
                .build();
    }

    private LoginVO loginAfterRegister(SysMember member) {
        LoginVO vo = doLogin(member);
        return vo;
    }

    private LoginVO doLogin(SysMember member) {
        int maxDevices = resolveMaxDevices(member.getVipLevel());
        StpMemberUtil.login(member.getId());
        memberSessionService.enforceDeviceLimit(member.getId(), maxDevices);

        member.setLastLoginTime(LocalDateTime.now());
        sysMemberMapper.updateById(member);

        return LoginVO.builder()
                .id(member.getId())
                .username(member.getUsername())
                .token(StpMemberUtil.getTokenValue())
                .tokenType("Bearer")
                .build();
    }

    private int resolveMaxDevices(Integer vipLevel) {
        if (vipLevel != null && vipLevel >= AuthConstants.VIP_LEVEL_VIP) {
            return authProperties.getMemberMaxDevicesVip();
        }
        return authProperties.getMemberMaxDevicesNormal();
    }

    private void ensureUsernameAvailable(String username) {
        Long count = sysMemberMapper.selectCount(new LambdaQueryWrapper<SysMember>()
                .eq(SysMember::getUsername, username));
        if (count != null && count > 0) {
            throw new BusinessException(CommonErrorCode.DUPLICATE_ENTRY);
        }
    }

    private void ensurePhoneAvailable(String phone) {
        Long count = sysMemberMapper.selectCount(new LambdaQueryWrapper<SysMember>()
                .eq(SysMember::getPhone, phone));
        if (count != null && count > 0) {
            throw new BusinessException(CommonErrorCode.DUPLICATE_ENTRY);
        }
    }

    private void ensureEmailAvailable(String email) {
        Long count = sysMemberMapper.selectCount(new LambdaQueryWrapper<SysMember>()
                .eq(SysMember::getEmail, email));
        if (count != null && count > 0) {
            throw new BusinessException(CommonErrorCode.DUPLICATE_ENTRY);
        }
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
