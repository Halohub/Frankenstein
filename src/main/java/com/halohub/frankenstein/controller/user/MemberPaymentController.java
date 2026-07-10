package com.halohub.frankenstein.controller.user;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.halohub.frankenstein.common.enums.PaymentChannel;
import com.halohub.frankenstein.common.result.Result;
import com.halohub.frankenstein.dto.PaymentCallbackRequest;
import com.halohub.frankenstein.dto.PaymentCreateRequest;
import com.halohub.frankenstein.service.PaymentService;
import com.halohub.frankenstein.vo.PaymentVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/api_v1/payment")
public class MemberPaymentController {

    private final PaymentService paymentService;

    public MemberPaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create")
    @SaCheckLogin(type = "user")
    @SaCheckPermission(value = "member:payment:pay", type = "user")
    public Result<PaymentVO> createPayment(@Valid @RequestBody PaymentCreateRequest request) {
        return Result.success(paymentService.createPayment(request));
    }

    @GetMapping("/{paymentNo}/status")
    @SaCheckLogin(type = "user")
    @SaCheckPermission(value = "member:payment:pay", type = "user")
    public Result<PaymentVO> paymentStatus(@PathVariable String paymentNo) {
        return Result.success(paymentService.getPaymentStatus(paymentNo));
    }

    @PostMapping("/callback/mock")
    public Result<PaymentVO> mockCallback(@Valid @RequestBody PaymentCallbackRequest request) {
        return Result.success(paymentService.handleCallback(PaymentChannel.MOCK, request));
    }

    @PostMapping("/callback/alipay")
    public Result<PaymentVO> alipayCallback(@Valid @RequestBody PaymentCallbackRequest request) {
        return Result.success(paymentService.handleCallback(PaymentChannel.ALIPAY, request));
    }

    @PostMapping("/callback/wxpay")
    public Result<PaymentVO> wxpayCallback(@Valid @RequestBody PaymentCallbackRequest request) {
        return Result.success(paymentService.handleCallback(PaymentChannel.WXPAY, request));
    }

    @PostMapping("/callback/stripe")
    public Result<PaymentVO> stripeCallback(@Valid @RequestBody PaymentCallbackRequest request) {
        return Result.success(paymentService.handleCallback(PaymentChannel.STRIPE, request));
    }
}
