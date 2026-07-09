package com.halohub.frankenstein.controller.user;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.halohub.frankenstein.common.result.Result;
import com.halohub.frankenstein.service.MemberProductService;
import com.halohub.frankenstein.vo.PageResult;
import com.halohub.frankenstein.vo.SkuVO;
import com.halohub.frankenstein.vo.SpuDetailVO;
import com.halohub.frankenstein.vo.SpuVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/api_v1/product")
public class MemberProductController {

    private final MemberProductService memberProductService;

    public MemberProductController(MemberProductService memberProductService) {
        this.memberProductService = memberProductService;
    }

    @GetMapping("/spu/list")
    @SaCheckLogin(type = "user")
    @SaCheckPermission(value = "member:product:spu:list", type = "user")
    public Result<PageResult<SpuVO>> pageSpu(@RequestParam(defaultValue = "1") int pageNum,
                                             @RequestParam(defaultValue = "10") int pageSize,
                                             @RequestParam(required = false) Long categoryId,
                                             @RequestParam(required = false) String keyword) {
        return Result.success(memberProductService.pageOnShelfSpu(pageNum, pageSize, categoryId, keyword));
    }

    @GetMapping("/spu/{id}")
    @SaCheckLogin(type = "user")
    @SaCheckPermission(value = "member:product:spu:detail", type = "user")
    public Result<SpuDetailVO> spuDetail(@PathVariable Long id) {
        return Result.success(memberProductService.getOnShelfSpuDetail(id));
    }

    @GetMapping("/sku/{id}")
    @SaCheckLogin(type = "user")
    @SaCheckPermission(value = "member:product:sku:detail", type = "user")
    public Result<SkuVO> skuDetail(@PathVariable Long id) {
        return Result.success(memberProductService.getActiveSkuDetail(id));
    }
}
