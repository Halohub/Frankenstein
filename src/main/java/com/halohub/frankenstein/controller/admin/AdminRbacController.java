package com.halohub.frankenstein.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.halohub.frankenstein.common.i18n.RequestLocaleResolver;
import com.halohub.frankenstein.common.result.Result;
import com.halohub.frankenstein.dto.RoleSaveRequest;
import com.halohub.frankenstein.dto.RoleUpdateRequest;
import com.halohub.frankenstein.service.AdminMenuService;
import com.halohub.frankenstein.service.AdminRoleService;
import com.halohub.frankenstein.vo.PageResult;
import com.halohub.frankenstein.vo.PermissionTreeVO;
import com.halohub.frankenstein.vo.RoleDetailVO;
import com.halohub.frankenstein.vo.RoleVO;
import jakarta.servlet.http.HttpServletRequest;
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

import java.util.List;

@RestController
@RequestMapping("/admin/api_v1")
public class AdminRbacController {

    private final AdminRoleService adminRoleService;
    private final AdminMenuService adminMenuService;

    public AdminRbacController(AdminRoleService adminRoleService, AdminMenuService adminMenuService) {
        this.adminRoleService = adminRoleService;
        this.adminMenuService = adminMenuService;
    }

    @GetMapping("/permission/tree")
    @SaCheckLogin(type = "admin")
    @SaCheckPermission(value = "admin:system:permission:tree", type = "admin")
    public Result<List<PermissionTreeVO>> permissionTree(HttpServletRequest request) {
        return Result.success(adminMenuService.listPermissionTree(RequestLocaleResolver.resolve(request)));
    }

    @GetMapping("/role")
    @SaCheckLogin(type = "admin")
    @SaCheckPermission(value = "admin:system:role:list", type = "admin")
    public Result<PageResult<RoleVO>> pageRoles(@RequestParam(defaultValue = "1") int pageNum,
                                                @RequestParam(defaultValue = "10") int pageSize,
                                                @RequestParam(required = false) String roleCode,
                                                @RequestParam(required = false) String roleName) {
        return Result.success(adminRoleService.pageRoles(pageNum, pageSize, roleCode, roleName));
    }

    @GetMapping("/role/{id}")
    @SaCheckLogin(type = "admin")
    @SaCheckPermission(value = "admin:system:role:detail", type = "admin")
    public Result<RoleDetailVO> roleDetail(@PathVariable Long id) {
        return Result.success(adminRoleService.getRoleDetail(id));
    }

    @PostMapping("/role")
    @SaCheckLogin(type = "admin")
    @SaCheckPermission(value = "admin:system:role:create", type = "admin")
    public Result<RoleDetailVO> createRole(@Valid @RequestBody RoleSaveRequest request) {
        return Result.success(adminRoleService.createRole(request));
    }

    @PutMapping("/role/{id}")
    @SaCheckLogin(type = "admin")
    @SaCheckPermission(value = "admin:system:role:update", type = "admin")
    public Result<RoleDetailVO> updateRole(@PathVariable Long id,
                                           @Valid @RequestBody RoleUpdateRequest request) {
        return Result.success(adminRoleService.updateRole(id, request));
    }

    @DeleteMapping("/role/{id}")
    @SaCheckLogin(type = "admin")
    @SaCheckPermission(value = "admin:system:role:delete", type = "admin")
    public Result<Void> deleteRole(@PathVariable Long id) {
        adminRoleService.deleteRole(id);
        return Result.success();
    }
}
