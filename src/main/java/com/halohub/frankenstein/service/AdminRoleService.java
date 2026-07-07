package com.halohub.frankenstein.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.halohub.frankenstein.common.constant.AuthConstants;
import com.halohub.frankenstein.common.constant.PermissionConstants;
import com.halohub.frankenstein.common.enums.CommonErrorCode;
import com.halohub.frankenstein.common.exception.BusinessException;
import com.halohub.frankenstein.dto.RoleSaveRequest;
import com.halohub.frankenstein.dto.RoleUpdateRequest;
import com.halohub.frankenstein.entity.SysPermission;
import com.halohub.frankenstein.entity.SysRole;
import com.halohub.frankenstein.mapper.SysPermissionMapper;
import com.halohub.frankenstein.mapper.SysRoleMapper;
import com.halohub.frankenstein.mapper.SysRolePermissionMapper;
import com.halohub.frankenstein.vo.PageResult;
import com.halohub.frankenstein.vo.RoleDetailVO;
import com.halohub.frankenstein.vo.RoleVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;

@Service
public class AdminRoleService {

    private final SysRoleMapper sysRoleMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;
    private final SysPermissionMapper sysPermissionMapper;

    public AdminRoleService(SysRoleMapper sysRoleMapper,
                            SysRolePermissionMapper sysRolePermissionMapper,
                            SysPermissionMapper sysPermissionMapper) {
        this.sysRoleMapper = sysRoleMapper;
        this.sysRolePermissionMapper = sysRolePermissionMapper;
        this.sysPermissionMapper = sysPermissionMapper;
    }

    public PageResult<RoleVO> pageRoles(int pageNum, int pageSize, String roleCode, String roleName) {
        Page<SysRole> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleScope, AuthConstants.ROLE_SCOPE_ADMIN)
                .like(StringUtils.hasText(roleCode), SysRole::getRoleCode, roleCode)
                .like(StringUtils.hasText(roleName), SysRole::getRoleName, roleName)
                .orderByAsc(SysRole::getId);
        Page<SysRole> result = sysRoleMapper.selectPage(page, wrapper);
        List<RoleVO> list = result.getRecords().stream().map(this::toRoleVO).toList();
        return PageResult.of(list, result.getTotal());
    }

    public RoleDetailVO getRoleDetail(Long roleId) {
        SysRole role = getAdminRoleOrThrow(roleId);
        RoleDetailVO detail = new RoleDetailVO();
        BeanUtils.copyProperties(toRoleVO(role), detail);
        detail.setPermissionIds(sysRolePermissionMapper.listPermissionIdsByRoleId(roleId));
        return detail;
    }

    @Transactional(rollbackFor = Exception.class)
    public RoleDetailVO createRole(RoleSaveRequest request) {
        assertRoleCodeAvailable(request.getRoleCode());
        validatePermissionIds(request.getPermissionIds());

        SysRole role = new SysRole();
        role.setRoleCode(request.getRoleCode());
        role.setRoleName(request.getRoleName());
        role.setRoleScope(AuthConstants.ROLE_SCOPE_ADMIN);
        role.setRemark(request.getRemark());
        role.setStatus(request.getStatus());
        sysRoleMapper.insert(role);

        assignPermissions(role.getId(), request.getPermissionIds());
        return getRoleDetail(role.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public RoleDetailVO updateRole(Long roleId, RoleUpdateRequest request) {
        SysRole role = getAdminRoleOrThrow(roleId);
        assertMutableRole(role);

        role.setRoleName(request.getRoleName());
        role.setRemark(request.getRemark());
        role.setStatus(request.getStatus());
        sysRoleMapper.updateById(role);

        validatePermissionIds(request.getPermissionIds());
        assignPermissions(roleId, request.getPermissionIds());
        return getRoleDetail(roleId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long roleId) {
        SysRole role = getAdminRoleOrThrow(roleId);
        assertMutableRole(role);
        if (sysRolePermissionMapper.countAdminsByRoleId(roleId) > 0) {
            throw new BusinessException(CommonErrorCode.DELETION_NOT_ALLOWED);
        }
        sysRolePermissionMapper.deleteByRoleId(roleId);
        sysRoleMapper.deleteById(roleId);
    }

    private void assignPermissions(Long roleId, List<Long> permissionIds) {
        sysRolePermissionMapper.deleteByRoleId(roleId);
        if (permissionIds == null || permissionIds.isEmpty()) {
            return;
        }
        sysRolePermissionMapper.batchInsert(roleId, permissionIds);
    }

    private void validatePermissionIds(List<Long> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            throw new BusinessException(CommonErrorCode.PARAM_CANNOT_BE_EMPTY);
        }
        List<SysPermission> permissions = sysPermissionMapper.selectBatchIds(permissionIds);
        if (permissions.size() != new HashSet<>(permissionIds).size()) {
            throw new BusinessException(CommonErrorCode.NO_CORRESPONDING_DATA);
        }
        boolean invalid = permissions.stream()
                .anyMatch(permission -> permission.getPermCode() == null
                        || !permission.getPermCode().startsWith(PermissionConstants.ADMIN_PERM_PREFIX));
        if (invalid) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED_OPERATION);
        }
    }

    private void assertRoleCodeAvailable(String roleCode) {
        Long count = sysRoleMapper.selectCount(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, roleCode));
        if (count != null && count > 0) {
            throw new BusinessException(CommonErrorCode.NAME_ALREADY_EXIST);
        }
    }

    private SysRole getAdminRoleOrThrow(Long roleId) {
        SysRole role = sysRoleMapper.selectById(roleId);
        if (role == null || !AuthConstants.ROLE_SCOPE_ADMIN.equals(role.getRoleScope())) {
            throw new BusinessException(CommonErrorCode.NO_CORRESPONDING_DATA);
        }
        return role;
    }

    private void assertMutableRole(SysRole role) {
        if (PermissionConstants.ROLE_ADMIN_SUPER.equals(role.getRoleCode())) {
            throw new BusinessException(CommonErrorCode.DELETION_NOT_ALLOWED);
        }
    }

    private RoleVO toRoleVO(SysRole role) {
        RoleVO roleVO = new RoleVO();
        BeanUtils.copyProperties(role, roleVO);
        return roleVO;
    }
}
