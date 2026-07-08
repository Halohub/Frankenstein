package com.halohub.frankenstein.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.halohub.frankenstein.common.constant.AuthConstants;
import com.halohub.frankenstein.common.constant.PermissionConstants;
import com.halohub.frankenstein.common.enums.CommonErrorCode;
import com.halohub.frankenstein.common.exception.BusinessException;
import com.halohub.frankenstein.dto.AdminCreateRequest;
import com.halohub.frankenstein.dto.AdminUpdateRequest;
import com.halohub.frankenstein.entity.SysAdmin;
import com.halohub.frankenstein.entity.SysRole;
import com.halohub.frankenstein.mapper.SysAdminMapper;
import com.halohub.frankenstein.mapper.SysAdminRoleMapper;
import com.halohub.frankenstein.mapper.SysRoleMapper;
import com.halohub.frankenstein.satoken.StpAdminUtil;
import com.halohub.frankenstein.vo.AdminDetailVO;
import com.halohub.frankenstein.vo.AdminVO;
import com.halohub.frankenstein.vo.PageResult;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;

@Service
public class AdminAccountService {

    private static final long BUILTIN_SUPER_ADMIN_ID = 1L;

    private final SysAdminMapper sysAdminMapper;
    private final SysAdminRoleMapper sysAdminRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final PasswordEncoder passwordEncoder;

    public AdminAccountService(SysAdminMapper sysAdminMapper,
                               SysAdminRoleMapper sysAdminRoleMapper,
                               SysRoleMapper sysRoleMapper,
                               PasswordEncoder passwordEncoder) {
        this.sysAdminMapper = sysAdminMapper;
        this.sysAdminRoleMapper = sysAdminRoleMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public PageResult<AdminVO> pageAdmins(int pageNum, int pageSize,
                                          String username, String nickname, Integer status) {
        Page<SysAdmin> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysAdmin> wrapper = new LambdaQueryWrapper<SysAdmin>()
                .like(StringUtils.hasText(username), SysAdmin::getUsername, username)
                .like(StringUtils.hasText(nickname), SysAdmin::getNickname, nickname)
                .eq(status != null, SysAdmin::getStatus, status)
                .orderByAsc(SysAdmin::getId);
        Page<SysAdmin> result = sysAdminMapper.selectPage(page, wrapper);
        return PageResult.of(result.getRecords().stream().map(this::toAdminVO).toList(), result.getTotal());
    }

    public AdminDetailVO getAdminDetail(Long adminId) {
        SysAdmin admin = getAdminOrThrow(adminId);
        AdminDetailVO detail = new AdminDetailVO();
        BeanUtils.copyProperties(toAdminVO(admin), detail);
        detail.setRoleIds(sysAdminRoleMapper.listRoleIdsByAdminId(adminId));
        return detail;
    }

    @Transactional(rollbackFor = Exception.class)
    public AdminDetailVO createAdmin(AdminCreateRequest request) {
        ensureUsernameAvailable(request.getUsername());
        validateAdminRoleIds(request.getRoleIds());

        SysAdmin admin = new SysAdmin();
        admin.setUsername(request.getUsername().trim());
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setNickname(StringUtils.hasText(request.getNickname())
                ? request.getNickname().trim()
                : request.getUsername().trim());
        admin.setPhone(blankToNull(request.getPhone()));
        admin.setEmail(blankToNull(request.getEmail()));
        admin.setStatus(request.getStatus());
        sysAdminMapper.insert(admin);

        assignRoles(admin.getId(), request.getRoleIds());
        return getAdminDetail(admin.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public AdminDetailVO updateAdmin(Long adminId, AdminUpdateRequest request) {
        SysAdmin admin = getAdminOrThrow(adminId);
        validateAdminRoleIds(request.getRoleIds());
        assertSuperAdminRolePreserved(adminId, request.getRoleIds());

        admin.setNickname(blankToNull(request.getNickname()));
        admin.setPhone(blankToNull(request.getPhone()));
        admin.setEmail(blankToNull(request.getEmail()));
        admin.setStatus(request.getStatus());
        if (StringUtils.hasText(request.getPassword())) {
            admin.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        sysAdminMapper.updateById(admin);

        assignRoles(adminId, request.getRoleIds());
        return getAdminDetail(adminId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteAdmin(Long adminId) {
        getAdminOrThrow(adminId);
        assertDeletable(adminId);
        sysAdminRoleMapper.deleteByAdminId(adminId);
        sysAdminMapper.deleteById(adminId);
    }

    private void assignRoles(Long adminId, List<Long> roleIds) {
        sysAdminRoleMapper.deleteByAdminId(adminId);
        sysAdminRoleMapper.batchInsert(adminId, roleIds);
    }

    private void validateAdminRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            throw new BusinessException(CommonErrorCode.PARAM_CANNOT_BE_EMPTY);
        }
        List<SysRole> roles = sysRoleMapper.selectBatchIds(roleIds);
        if (roles.size() != new HashSet<>(roleIds).size()) {
            throw new BusinessException(CommonErrorCode.NO_CORRESPONDING_DATA);
        }
        boolean invalid = roles.stream()
                .anyMatch(role -> !AuthConstants.ROLE_SCOPE_ADMIN.equals(role.getRoleScope()));
        if (invalid) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED_OPERATION);
        }
    }

    private void assertSuperAdminRolePreserved(Long adminId, List<Long> roleIds) {
        if (adminId != BUILTIN_SUPER_ADMIN_ID) {
            return;
        }
        SysRole superRole = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, PermissionConstants.ROLE_ADMIN_SUPER)
                .last("LIMIT 1"));
        if (superRole != null && !roleIds.contains(superRole.getId())) {
            throw new BusinessException(CommonErrorCode.DELETION_NOT_ALLOWED);
        }
    }

    private void assertDeletable(Long adminId) {
        if (adminId == BUILTIN_SUPER_ADMIN_ID) {
            throw new BusinessException(CommonErrorCode.DELETION_NOT_ALLOWED);
        }
        if (StpAdminUtil.isLogin() && adminId == StpAdminUtil.getLoginIdAsLong()) {
            throw new BusinessException(CommonErrorCode.DELETION_NOT_ALLOWED);
        }
        if (sysAdminRoleMapper.countRoleByAdminIdAndCode(adminId, PermissionConstants.ROLE_ADMIN_SUPER) > 0
                && sysAdminRoleMapper.countAdminsByRoleCode(PermissionConstants.ROLE_ADMIN_SUPER) <= 1) {
            throw new BusinessException(CommonErrorCode.DELETION_NOT_ALLOWED);
        }
    }

    private void ensureUsernameAvailable(String username) {
        Long count = sysAdminMapper.selectCount(new LambdaQueryWrapper<SysAdmin>()
                .eq(SysAdmin::getUsername, username.trim()));
        if (count != null && count > 0) {
            throw new BusinessException(CommonErrorCode.DUPLICATE_ENTRY);
        }
    }

    private SysAdmin getAdminOrThrow(Long adminId) {
        SysAdmin admin = sysAdminMapper.selectById(adminId);
        if (admin == null) {
            throw new BusinessException(CommonErrorCode.NO_CORRESPONDING_DATA);
        }
        return admin;
    }

    private AdminVO toAdminVO(SysAdmin admin) {
        AdminVO vo = new AdminVO();
        BeanUtils.copyProperties(admin, vo);
        vo.setRoles(sysAdminMapper.listRoleCodesByAdminId(admin.getId()));
        return vo;
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
