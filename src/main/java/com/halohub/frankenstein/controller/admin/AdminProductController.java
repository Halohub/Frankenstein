package com.halohub.frankenstein.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.halohub.frankenstein.common.result.Result;
import com.halohub.frankenstein.dto.SpuCreateRequest;
import com.halohub.frankenstein.dto.SpuStatusUpdateRequest;
import com.halohub.frankenstein.dto.SpuUpdateRequest;
import com.halohub.frankenstein.service.AdminProductService;
import com.halohub.frankenstein.vo.PageResult;
import com.halohub.frankenstein.vo.SpuDetailVO;
import com.halohub.frankenstein.vo.SpuVO;
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
@RequestMapping("/admin/api_v1/product/spu")
public class AdminProductController {

    private final AdminProductService adminProductService;

    public AdminProductController(AdminProductService adminProductService) {
        this.adminProductService = adminProductService;
    }

    @GetMapping("/list")
    @SaCheckLogin(type = "admin")
    @SaCheckPermission(value = "admin:product:spu:list", type = "admin")
    public Result<PageResult<SpuVO>> pageSpu(@RequestParam(defaultValue = "1") int pageNum,
                                             @RequestParam(defaultValue = "10") int pageSize,
                                             @RequestParam(required = false) Long categoryId,
                                             @RequestParam(required = false) String title,
                                             @RequestParam(required = false) Integer status) {
        return Result.success(adminProductService.pageSpu(pageNum, pageSize, categoryId, title, status));
    }

    @GetMapping("/{id}")
    @SaCheckLogin(type = "admin")
    @SaCheckPermission(value = "admin:product:spu:detail", type = "admin")
    public Result<SpuDetailVO> spuDetail(@PathVariable Long id) {
        return Result.success(adminProductService.getSpuDetail(id));
    }

    @PostMapping
    @SaCheckLogin(type = "admin")
    @SaCheckPermission(value = "admin:product:spu:create", type = "admin")
    public Result<SpuDetailVO> createSpu(@Valid @RequestBody SpuCreateRequest request) {
        return Result.success(adminProductService.createSpu(request));
    }

    @PutMapping("/{id}")
    @SaCheckLogin(type = "admin")
    @SaCheckPermission(value = "admin:product:spu:update", type = "admin")
    public Result<SpuDetailVO> updateSpu(@PathVariable Long id,
                                         @Valid @RequestBody SpuUpdateRequest request) {
        return Result.success(adminProductService.updateSpu(id, request));
    }

    @PutMapping("/{id}/status")
    @SaCheckLogin(type = "admin")
    @SaCheckPermission(value = "admin:product:spu:status", type = "admin")
    public Result<SpuDetailVO> updateSpuStatus(@PathVariable Long id,
                                             @Valid @RequestBody SpuStatusUpdateRequest request) {
        return Result.success(adminProductService.updateSpuStatus(id, request.getStatus()));
    }

    @DeleteMapping("/{id}")
    @SaCheckLogin(type = "admin")
    @SaCheckPermission(value = "admin:product:spu:remove", type = "admin")
    public Result<Void> deleteSpu(@PathVariable Long id) {
        adminProductService.deleteSpu(id);
        return Result.success();
    }
}
