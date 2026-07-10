package com.halohub.frankenstein.controller.user;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.halohub.frankenstein.common.result.Result;
import com.halohub.frankenstein.dto.OrderCreateRequest;
import com.halohub.frankenstein.service.MemberOrderService;
import com.halohub.frankenstein.vo.OrderDetailVO;
import com.halohub.frankenstein.vo.OrderPreviewVO;
import com.halohub.frankenstein.vo.OrderVO;
import com.halohub.frankenstein.vo.PageResult;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/api_v1/order")
public class MemberOrderController {

    private final MemberOrderService memberOrderService;

    public MemberOrderController(MemberOrderService memberOrderService) {
        this.memberOrderService = memberOrderService;
    }

    @PostMapping("/preview")
    @SaCheckLogin(type = "user")
    @SaCheckPermission(value = "member:order:create", type = "user")
    public Result<OrderPreviewVO> previewOrder(@Valid @RequestBody OrderCreateRequest request) {
        return Result.success(memberOrderService.previewOrder(request));
    }

    @PostMapping
    @SaCheckLogin(type = "user")
    @SaCheckPermission(value = "member:order:create", type = "user")
    public Result<OrderDetailVO> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        return Result.success(memberOrderService.createOrder(request));
    }

    @GetMapping("/list")
    @SaCheckLogin(type = "user")
    @SaCheckPermission(value = "member:order:view", type = "user")
    public Result<PageResult<OrderVO>> pageOrders(@RequestParam(defaultValue = "1") int pageNum,
                                                  @RequestParam(defaultValue = "10") int pageSize,
                                                  @RequestParam(required = false) String status) {
        return Result.success(memberOrderService.pageOrders(pageNum, pageSize, status));
    }

    @GetMapping("/{id}")
    @SaCheckLogin(type = "user")
    @SaCheckPermission(value = "member:order:detail", type = "user")
    public Result<OrderDetailVO> orderDetail(@PathVariable Long id) {
        return Result.success(memberOrderService.getOrderDetail(id));
    }

    @PostMapping("/{id}/cancel")
    @SaCheckLogin(type = "user")
    @SaCheckPermission(value = "member:order:cancel", type = "user")
    public Result<OrderDetailVO> cancelOrder(@PathVariable Long id) {
        return Result.success(memberOrderService.cancelOrder(id));
    }
}
