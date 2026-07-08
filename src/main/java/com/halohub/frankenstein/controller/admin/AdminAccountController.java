package com.halohub.frankenstein.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.halohub.frankenstein.common.result.Result;
import com.halohub.frankenstein.dto.AdminCreateRequest;
import com.halohub.frankenstein.dto.AdminUpdateRequest;
import com.halohub.frankenstein.service.AdminAccountService;
import com.halohub.frankenstein.vo.AdminDetailVO;
import com.halohub.frankenstein.vo.AdminVO;
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
@RequestMapping("/admin/api_v1/admin")
public class AdminAccountController {

    private final AdminAccountService adminAccountService;

    public AdminAccountController(AdminAccountService adminAccountService) {
        this.adminAccountService = adminAccountService;
    }

    @GetMapping("/list")
    @SaCheckLogin(type = "admin")
    @SaCheckPermission(value = "admin:system:admin:list", type = "admin")
    public Result<PageResult<AdminVO>> pageAdmins(@RequestParam(defaultValue = "1") int pageNum,
                                                  @RequestParam(defaultValue = "10") int pageSize,
                                                  @RequestParam(required = false) String username,
                                                  @RequestParam(required = false) String nickname,
                                                  @RequestParam(required = false) Integer status) {
        return Result.success(adminAccountService.pageAdmins(pageNum, pageSize, username, nickname, status));
    }

    @GetMapping("/{id}")
    @SaCheckLogin(type = "admin")
    @SaCheckPermission(value = "admin:system:admin:detail", type = "admin")
    public Result<AdminDetailVO> adminDetail(@PathVariable Long id) {
        return Result.success(adminAccountService.getAdminDetail(id));
    }

    @PostMapping
    @SaCheckLogin(type = "admin")
    @SaCheckPermission(value = "admin:system:admin:create", type = "admin")
    public Result<AdminDetailVO> createAdmin(@Valid @RequestBody AdminCreateRequest request) {
        return Result.success(adminAccountService.createAdmin(request));
    }

    @PutMapping("/{id}")
    @SaCheckLogin(type = "admin")
    @SaCheckPermission(value = "admin:system:admin:update", type = "admin")
    public Result<AdminDetailVO> updateAdmin(@PathVariable Long id,
                                             @Valid @RequestBody AdminUpdateRequest request) {
        return Result.success(adminAccountService.updateAdmin(id, request));
    }

    @DeleteMapping("/{id}")
    @SaCheckLogin(type = "admin")
    @SaCheckPermission(value = "admin:system:admin:remove", type = "admin")
    public Result<Void> deleteAdmin(@PathVariable Long id) {
        adminAccountService.deleteAdmin(id);
        return Result.success();
    }
}
