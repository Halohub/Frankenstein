package com.halohub.frankenstein.controller.user;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.halohub.frankenstein.common.result.Result;
import com.halohub.frankenstein.dto.LoginRequest;
import com.halohub.frankenstein.dto.MemberRegisterRequest;
import com.halohub.frankenstein.service.MemberAuthService;
import com.halohub.frankenstein.vo.AuthInfoVO;
import com.halohub.frankenstein.vo.LoginVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/api_v1/auth")
public class MemberAuthController {

    private final MemberAuthService memberAuthService;

    public MemberAuthController(MemberAuthService memberAuthService) {
        this.memberAuthService = memberAuthService;
    }

    @PostMapping("/register")
    public Result<LoginVO> register(@Valid @RequestBody MemberRegisterRequest request) {
        return Result.success(memberAuthService.register(request));
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(memberAuthService.login(request));
    }

    @PostMapping("/logout")
    @SaCheckLogin(type = "user")
    public Result<Void> logout() {
        memberAuthService.logout();
        return Result.success();
    }

    @GetMapping("/info")
    @SaCheckLogin(type = "user")
    public Result<AuthInfoVO> info() {
        return Result.success(memberAuthService.currentUser());
    }
}
