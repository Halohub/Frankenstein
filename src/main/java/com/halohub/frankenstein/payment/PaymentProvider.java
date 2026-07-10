package com.halohub.frankenstein.payment;

import com.halohub.frankenstein.common.enums.PaymentChannel;
import com.halohub.frankenstein.dto.PaymentCallbackRequest;

public interface PaymentProvider {

    PaymentChannel channel();

    PaymentCreateResult create(PaymentContext context);

    PaymentCallbackResult handleCallback(PaymentCallbackRequest request);
}
