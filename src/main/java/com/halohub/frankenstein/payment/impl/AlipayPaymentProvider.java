package com.halohub.frankenstein.payment.impl;

import com.halohub.frankenstein.common.enums.CommonErrorCode;
import com.halohub.frankenstein.common.enums.PaymentChannel;
import com.halohub.frankenstein.common.exception.BusinessException;
import org.springframework.stereotype.Component;

@Component
public class AlipayPaymentProvider extends AbstractUnavailablePaymentProvider {

    @Override
    public PaymentChannel channel() {
        return PaymentChannel.ALIPAY;
    }

    @Override
    protected BusinessException channelUnavailable() {
        return new BusinessException(CommonErrorCode.PAYMENT_CHANNEL_UNAVAILABLE);
    }
}
