package com.halohub.frankenstein.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.halohub.frankenstein.common.result.Result;
import com.halohub.frankenstein.dto.LoginRequest;
import com.halohub.frankenstein.service.AdminAuthService;
import com.halohub.frankenstein.vo.AuthInfoVO;
import com.halohub.frankenstein.vo.LoginVO;
import com.halohub.frankenstein.vo.RouteMenuVO;
import com.halohub.frankenstein.common.i18n.RequestLocaleResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/api_v1/auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(adminAuthService.login(request));
    }

    @PostMapping("/logout")
    @SaCheckLogin(type = "admin")
    public Result<Void> logout() {
        adminAuthService.logout();
        return Result.success();
    }

    @GetMapping("/info")
    @SaCheckLogin(type = "admin")
    public Result<AuthInfoVO> info() {
        return Result.success(adminAuthService.currentUser());
    }

    @GetMapping("/menus")
    @SaCheckLogin(type = "admin")
    @SaCheckPermission(value = "admin:auth:menus", type = "admin")
    public Result<List<RouteMenuVO>> menus(HttpServletRequest request) {
        return Result.success(adminAuthService.currentMenus(RequestLocaleResolver.resolve(request)));
    }

    @GetMapping("/ping")
    @SaCheckLogin(type = "admin")
    @SaCheckRole(value = "ADMIN_SUPER", type = "admin")
    public Result<String> ping() {
        return Result.success("admin ok");
    }
}
