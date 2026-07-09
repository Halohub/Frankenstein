package com.halohub.frankenstein.controller.user;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.halohub.frankenstein.common.result.Result;
import com.halohub.frankenstein.service.MemberProductService;
import com.halohub.frankenstein.vo.CategoryTreeVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user/api_v1/category")
public class MemberCategoryController {

    private final MemberProductService memberProductService;

    public MemberCategoryController(MemberProductService memberProductService) {
        this.memberProductService = memberProductService;
    }

    @GetMapping("/tree")
    @SaCheckLogin(type = "user")
    @SaCheckPermission(value = "member:product:category:tree", type = "user")
    public Result<List<CategoryTreeVO>> categoryTree() {
        return Result.success(memberProductService.listActiveCategoryTree());
    }
}
