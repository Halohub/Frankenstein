package com.halohub.frankenstein.payment.impl;

import com.halohub.frankenstein.common.enums.CommonErrorCode;
import com.halohub.frankenstein.common.enums.PaymentChannel;
import com.halohub.frankenstein.common.enums.PaymentStatus;
import com.halohub.frankenstein.common.exception.BusinessException;
import com.halohub.frankenstein.dto.PaymentCallbackRequest;
import com.halohub.frankenstein.payment.PaymentCallbackResult;
import com.halohub.frankenstein.payment.PaymentContext;
import com.halohub.frankenstein.payment.PaymentCreateResult;
import org.springframework.stereotype.Component;

@Component
public class MockPaymentProvider implements com.halohub.frankenstein.payment.PaymentProvider {

    @Override
    public PaymentChannel channel() {
        return PaymentChannel.MOCK;
    }

    @Override
    public PaymentCreateResult create(PaymentContext context) {
        return PaymentCreateResult.builder()
                .status(PaymentStatus.PENDING)
                .clientPayload("{\"mock\":true,\"paymentNo\":\"" + context.getPayment().getPaymentNo() + "\"}")
                .build();
    }

    @Override
    public PaymentCallbackResult handleCallback(PaymentCallbackRequest request) {
        return PaymentCallbackResult.builder()
                .handled(true)
                .status(PaymentStatus.SUCCESS)
                .thirdPartyNo("MOCK-" + request.getPaymentNo())
                .callbackRaw("{\"paymentNo\":\"" + request.getPaymentNo() + "\"}")
                .build();
    }
}
