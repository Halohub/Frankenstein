package com.halohub.frankenstein.payment.impl;

import com.halohub.frankenstein.common.enums.PaymentChannel;
import com.halohub.frankenstein.common.enums.PaymentStatus;
import com.halohub.frankenstein.common.exception.BusinessException;
import com.halohub.frankenstein.dto.PaymentCallbackRequest;
import com.halohub.frankenstein.payment.PaymentContext;
import com.halohub.frankenstein.payment.PaymentCreateResult;
import com.halohub.frankenstein.payment.PaymentCallbackResult;
import com.halohub.frankenstein.payment.PaymentProvider;

public abstract class AbstractUnavailablePaymentProvider implements PaymentProvider {

    @Override
    public PaymentCreateResult create(PaymentContext context) {
        throw channelUnavailable();
    }

    @Override
    public PaymentCallbackResult handleCallback(PaymentCallbackRequest request) {
        throw channelUnavailable();
    }

    protected abstract BusinessException channelUnavailable();
}
