package com.halohub.frankenstein.controller.user;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.halohub.frankenstein.common.result.Result;
import com.halohub.frankenstein.dto.CartAddRequest;
import com.halohub.frankenstein.dto.CartSelectRequest;
import com.halohub.frankenstein.dto.CartUpdateRequest;
import com.halohub.frankenstein.service.MemberCartService;
import com.halohub.frankenstein.vo.CartListVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/api_v1/cart")
public class MemberCartController {

    private final MemberCartService memberCartService;

    public MemberCartController(MemberCartService memberCartService) {
        this.memberCartService = memberCartService;
    }

    @GetMapping("/list")
    @SaCheckLogin(type = "user")
    @SaCheckPermission(value = "member:cart:view", type = "user")
    public Result<CartListVO> listCart() {
        return Result.success(memberCartService.listCart());
    }

    @PostMapping("/item")
    @SaCheckLogin(type = "user")
    @SaCheckPermission(value = "member:cart:add", type = "user")
    public Result<CartListVO> addItem(@Valid @RequestBody CartAddRequest request) {
        return Result.success(memberCartService.addItem(request));
    }

    @PutMapping("/item/{id}")
    @SaCheckLogin(type = "user")
    @SaCheckPermission(value = "member:cart:update", type = "user")
    public Result<CartListVO> updateItem(@PathVariable Long id,
                                         @Valid @RequestBody CartUpdateRequest request) {
        return Result.success(memberCartService.updateItem(id, request));
    }

    @DeleteMapping("/item/{id}")
    @SaCheckLogin(type = "user")
    @SaCheckPermission(value = "member:cart:remove", type = "user")
    public Result<CartListVO> removeItem(@PathVariable Long id) {
        return Result.success(memberCartService.removeItem(id));
    }

    @PutMapping("/select")
    @SaCheckLogin(type = "user")
    @SaCheckPermission(value = "member:cart:select", type = "user")
    public Result<CartListVO> updateSelect(@Valid @RequestBody CartSelectRequest request) {
        return Result.success(memberCartService.updateSelect(request));
    }
}
