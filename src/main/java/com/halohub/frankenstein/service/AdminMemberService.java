package com.halohub.frankenstein.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.halohub.frankenstein.common.constant.AuthConstants;
import com.halohub.frankenstein.common.constant.PermissionConstants;
import com.halohub.frankenstein.common.enums.CommonErrorCode;
import com.halohub.frankenstein.common.exception.BusinessException;
import com.halohub.frankenstein.dto.MemberCreateRequest;
import com.halohub.frankenstein.dto.MemberPromoteRequest;
import com.halohub.frankenstein.dto.MemberUpdateRequest;
import com.halohub.frankenstein.entity.SysAdmin;
import com.halohub.frankenstein.entity.SysMember;
import com.halohub.frankenstein.entity.SysRole;
import com.halohub.frankenstein.mapper.SysAdminMapper;
import com.halohub.frankenstein.mapper.SysAdminRoleMapper;
import com.halohub.frankenstein.mapper.SysMemberMapper;
import com.halohub.frankenstein.mapper.SysRoleMapper;
import com.halohub.frankenstein.vo.MemberPromoteVO;
import com.halohub.frankenstein.vo.MemberVO;
import com.halohub.frankenstein.vo.PageResult;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;

@Service
public class AdminMemberService {

    private static final String DEFAULT_MEMBER_ROLE = "MEMBER_NORMAL";

    private final SysMemberMapper sysMemberMapper;
    private final SysAdminMapper sysAdminMapper;
    private final SysAdminRoleMapper sysAdminRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final PasswordEncoder passwordEncoder;

    public AdminMemberService(SysMemberMapper sysMemberMapper,
                              SysAdminMapper sysAdminMapper,
                              SysAdminRoleMapper sysAdminRoleMapper,
                              SysRoleMapper sysRoleMapper,
                              PasswordEncoder passwordEncoder) {
        this.sysMemberMapper = sysMemberMapper;
        this.sysAdminMapper = sysAdminMapper;
        this.sysAdminRoleMapper = sysAdminRoleMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public PageResult<MemberVO> pageMembers(int pageNum, int pageSize,
                                            String username, String phone, Integer status) {
        Page<SysMember> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysMember> wrapper = new LambdaQueryWrapper<SysMember>()
                .like(StringUtils.hasText(username), SysMember::getUsername, username)
                .like(StringUtils.hasText(phone), SysMember::getPhone, phone)
                .eq(status != null, SysMember::getStatus, status)
                .orderByDesc(SysMember::getId);
        Page<SysMember> result = sysMemberMapper.selectPage(page, wrapper);
        return PageResult.of(result.getRecords().stream().map(this::toMemberVO).toList(), result.getTotal());
    }

    public MemberVO getMemberDetail(Long memberId) {
        SysMember member = getMemberOrThrow(memberId);
        return toMemberVO(member);
    }

    @Transactional(rollbackFor = Exception.class)
    public MemberVO createMember(MemberCreateRequest request) {
        ensureUsernameAvailable(request.getUsername());
        ensurePhoneAvailable(blankToNull(request.getPhone()), null);
        ensureEmailAvailable(blankToNull(request.getEmail()), null);

        SysMember member = new SysMember();
        member.setUsername(request.getUsername().trim());
        member.setPassword(passwordEncoder.encode(request.getPassword()));
        member.setNickname(StringUtils.hasText(request.getNickname())
                ? request.getNickname().trim()
                : request.getUsername().trim());
        member.setPhone(blankToNull(request.getPhone()));
        member.setEmail(blankToNull(request.getEmail()));
        member.setVipLevel(AuthConstants.VIP_LEVEL_NORMAL);
        member.setStatus(request.getStatus());
        member.setRegisterSource("admin");
        sysMemberMapper.insert(member);

        sysMemberMapper.bindDefaultRole(member.getId(), DEFAULT_MEMBER_ROLE);
        return toMemberVO(member);
    }

    @Transactional(rollbackFor = Exception.class)
    public MemberVO updateMember(Long memberId, MemberUpdateRequest request) {
        SysMember member = getMemberOrThrow(memberId);
        ensurePhoneAvailable(blankToNull(request.getPhone()), memberId);
        ensureEmailAvailable(blankToNull(request.getEmail()), memberId);

        member.setNickname(blankToNull(request.getNickname()));
        member.setPhone(blankToNull(request.getPhone()));
        member.setEmail(blankToNull(request.getEmail()));
        member.setStatus(request.getStatus());
        member.setVipLevel(request.getVipLevel());
        sysMemberMapper.updateById(member);
        return toMemberVO(member);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteMember(Long memberId) {
        getMemberOrThrow(memberId);
        sysMemberMapper.deleteById(memberId);
    }

    @Transactional(rollbackFor = Exception.class)
    public MemberPromoteVO promoteMember(Long memberId, MemberPromoteRequest request) {
        SysMember member = getMemberOrThrow(memberId);
        ensureAdminUsernameAvailable(member.getUsername());
        validatePromoteRoleIds(request.getRoleIds());

        SysAdmin admin = new SysAdmin();
        admin.setUsername(member.getUsername());
        if (StringUtils.hasText(request.getPassword())) {
            admin.setPassword(passwordEncoder.encode(request.getPassword()));
        } else {
            admin.setPassword(member.getPassword());
        }
        admin.setNickname(StringUtils.hasText(member.getNickname()) ? member.getNickname() : member.getUsername());
        admin.setPhone(member.getPhone());
        admin.setEmail(member.getEmail());
        admin.setStatus(AuthConstants.STATUS_ACTIVE);
        sysAdminMapper.insert(admin);

        sysAdminRoleMapper.batchInsert(admin.getId(), request.getRoleIds());

        MemberPromoteVO result = new MemberPromoteVO();
        result.setMemberId(member.getId());
        result.setAdminId(admin.getId());
        result.setAdminUsername(admin.getUsername());
        return result;
    }

    private void validatePromoteRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            throw new BusinessException(CommonErrorCode.PARAM_CANNOT_BE_EMPTY);
        }
        List<SysRole> roles = sysRoleMapper.selectBatchIds(roleIds);
        if (roles.size() != new HashSet<>(roleIds).size()) {
            throw new BusinessException(CommonErrorCode.NO_CORRESPONDING_DATA);
        }
        boolean invalid = roles.stream()
                .anyMatch(role -> !AuthConstants.ROLE_SCOPE_ADMIN.equals(role.getRoleScope())
                        || PermissionConstants.ROLE_ADMIN_SUPER.equals(role.getRoleCode()));
        if (invalid) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED_OPERATION);
        }
    }

    private void ensureAdminUsernameAvailable(String username) {
        Long count = sysAdminMapper.selectCount(new LambdaQueryWrapper<SysAdmin>()
                .eq(SysAdmin::getUsername, username));
        if (count != null && count > 0) {
            throw new BusinessException(CommonErrorCode.DUPLICATE_ENTRY);
        }
    }

    private SysMember getMemberOrThrow(Long memberId) {
        SysMember member = sysMemberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException(CommonErrorCode.NO_CORRESPONDING_DATA);
        }
        return member;
    }

    private MemberVO toMemberVO(SysMember member) {
        MemberVO vo = new MemberVO();
        BeanUtils.copyProperties(member, vo);
        vo.setRoles(sysMemberMapper.listRoleCodesByMemberId(member.getId()));
        return vo;
    }

    private void ensureUsernameAvailable(String username) {
        Long count = sysMemberMapper.selectCount(new LambdaQueryWrapper<SysMember>()
                .eq(SysMember::getUsername, username.trim()));
        if (count != null && count > 0) {
            throw new BusinessException(CommonErrorCode.DUPLICATE_ENTRY);
        }
    }

    private void ensurePhoneAvailable(String phone, Long excludeMemberId) {
        if (!StringUtils.hasText(phone)) {
            return;
        }
        LambdaQueryWrapper<SysMember> wrapper = new LambdaQueryWrapper<SysMember>()
                .eq(SysMember::getPhone, phone);
        if (excludeMemberId != null) {
            wrapper.ne(SysMember::getId, excludeMemberId);
        }
        Long count = sysMemberMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BusinessException(CommonErrorCode.DUPLICATE_ENTRY);
        }
    }

    private void ensureEmailAvailable(String email, Long excludeMemberId) {
        if (!StringUtils.hasText(email)) {
            return;
        }
        LambdaQueryWrapper<SysMember> wrapper = new LambdaQueryWrapper<SysMember>()
                .eq(SysMember::getEmail, email);
        if (excludeMemberId != null) {
            wrapper.ne(SysMember::getId, excludeMemberId);
        }
        Long count = sysMemberMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BusinessException(CommonErrorCode.DUPLICATE_ENTRY);
        }
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
