package com.halohub.frankenstein.payment;

import com.halohub.frankenstein.common.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentCallbackResult {

    private boolean handled;
    private PaymentStatus status;
    private String thirdPartyNo;
    private String callbackRaw;
}
