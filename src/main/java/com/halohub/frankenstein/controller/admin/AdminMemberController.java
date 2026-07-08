package com.halohub.frankenstein.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.halohub.frankenstein.common.result.Result;
import com.halohub.frankenstein.dto.MemberCreateRequest;
import com.halohub.frankenstein.dto.MemberPromoteRequest;
import com.halohub.frankenstein.dto.MemberUpdateRequest;
import com.halohub.frankenstein.service.AdminMemberService;
import com.halohub.frankenstein.vo.MemberPromoteVO;
import com.halohub.frankenstein.vo.MemberVO;
import com.halohub.frankenstein.vo.PageResult;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/api_v1/member")
public class AdminMemberController {

    private final AdminMemberService adminMemberService;

    public AdminMemberController(AdminMemberService adminMemberService) {
        this.adminMemberService = adminMemberService;
    }

    @GetMapping("/list")
    @SaCheckLogin(type = "admin")
    @SaCheckPermission(value = "admin:member:query", type = "admin")
    public Result<PageResult<MemberVO>> pageMembers(@RequestParam(defaultValue = "1") int pageNum,
                                                    @RequestParam(defaultValue = "10") int pageSize,
                                                    @RequestParam(required = false) String username,
                                                    @RequestParam(required = false) String phone,
                                                    @RequestParam(required = false) Integer status) {
        return Result.success(adminMemberService.pageMembers(pageNum, pageSize, username, phone, status));
    }

    @GetMapping("/{id}")
    @SaCheckLogin(type = "admin")
    @SaCheckPermission(value = "admin:member:detail", type = "admin")
    public Result<MemberVO> memberDetail(@PathVariable Long id) {
        return Result.success(adminMemberService.getMemberDetail(id));
    }

    @PostMapping
    @SaCheckLogin(type = "admin")
    @SaCheckPermission(value = "admin:member:create", type = "admin")
    public Result<MemberVO> createMember(@Valid @RequestBody MemberCreateRequest request) {
        return Result.success(adminMemberService.createMember(request));
    }

    @PutMapping("/{id}")
    @SaCheckLogin(type = "admin")
    @SaCheckPermission(value = "admin:member:update", type = "admin")
    public Result<MemberVO> updateMember(@PathVariable Long id,
                                         @Valid @RequestBody MemberUpdateRequest request) {
        return Result.success(adminMemberService.updateMember(id, request));
    }

    @DeleteMapping("/{id}")
    @SaCheckLogin(type = "admin")
    @SaCheckPermission(value = "admin:member:remove", type = "admin")
    public Result<Void> deleteMember(@PathVariable Long id) {
        adminMemberService.deleteMember(id);
        return Result.success();
    }

    @PostMapping("/{id}/promote")
    @SaCheckLogin(type = "admin")
    @SaCheckPermission(value = "admin:member:promote", type = "admin")
    public Result<MemberPromoteVO> promoteMember(@PathVariable Long id,
                                                 @Valid @RequestBody MemberPromoteRequest request) {
        return Result.success(adminMemberService.promoteMember(id, request));
    }
}
