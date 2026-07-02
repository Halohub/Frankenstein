package com.halohub.frankenstein.satoken;

import cn.dev33.satoken.stp.StpInterface;
import com.halohub.frankenstein.common.constant.AuthConstants;
import com.halohub.frankenstein.mapper.SysAdminMapper;
import com.halohub.frankenstein.mapper.SysMemberMapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class StpInterfaceImpl implements StpInterface {

    private final SysAdminMapper sysAdminMapper;
    private final SysMemberMapper sysMemberMapper;

    public StpInterfaceImpl(SysAdminMapper sysAdminMapper, SysMemberMapper sysMemberMapper) {
        this.sysAdminMapper = sysAdminMapper;
        this.sysMemberMapper = sysMemberMapper;
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        if (AuthConstants.LOGIN_TYPE_ADMIN.equals(loginType)) {
            return sysAdminMapper.listPermCodesByAdminId(toLong(loginId));
        }
        if (AuthConstants.LOGIN_TYPE_MEMBER.equals(loginType)) {
            return sysMemberMapper.listPermCodesByMemberId(toLong(loginId));
        }
        return Collections.emptyList();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        if (AuthConstants.LOGIN_TYPE_ADMIN.equals(loginType)) {
            return sysAdminMapper.listRoleCodesByAdminId(toLong(loginId));
        }
        if (AuthConstants.LOGIN_TYPE_MEMBER.equals(loginType)) {
            return sysMemberMapper.listRoleCodesByMemberId(toLong(loginId));
        }
        return Collections.emptyList();
    }

    private Long toLong(Object loginId) {
        if (loginId instanceof Long l) {
            return l;
        }
        return Long.parseLong(String.valueOf(loginId));
    }
}
