package com.halohub.frankenstein.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.halohub.frankenstein.common.result.Result;
import com.halohub.frankenstein.dto.CategoryCreateRequest;
import com.halohub.frankenstein.dto.CategoryUpdateRequest;
import com.halohub.frankenstein.service.AdminCategoryService;
import com.halohub.frankenstein.vo.CategoryTreeVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/api_v1/category")
public class AdminCategoryController {

    private final AdminCategoryService adminCategoryService;

    public AdminCategoryController(AdminCategoryService adminCategoryService) {
        this.adminCategoryService = adminCategoryService;
    }

    @GetMapping("/tree")
    @SaCheckLogin(type = "admin")
    @SaCheckPermission(value = "admin:product:category:tree", type = "admin")
    public Result<List<CategoryTreeVO>> categoryTree() {
        return Result.success(adminCategoryService.listTree());
    }

    @PostMapping
    @SaCheckLogin(type = "admin")
    @SaCheckPermission(value = "admin:product:category:create", type = "admin")
    public Result<CategoryTreeVO> createCategory(@Valid @RequestBody CategoryCreateRequest request) {
        return Result.success(adminCategoryService.createCategory(request));
    }

    @PutMapping("/{id}")
    @SaCheckLogin(type = "admin")
    @SaCheckPermission(value = "admin:product:category:update", type = "admin")
    public Result<CategoryTreeVO> updateCategory(@PathVariable Long id,
                                                 @Valid @RequestBody CategoryUpdateRequest request) {
        return Result.success(adminCategoryService.updateCategory(id, request));
    }

    @DeleteMapping("/{id}")
    @SaCheckLogin(type = "admin")
    @SaCheckPermission(value = "admin:product:category:remove", type = "admin")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        adminCategoryService.deleteCategory(id);
        return Result.success();
    }
}
