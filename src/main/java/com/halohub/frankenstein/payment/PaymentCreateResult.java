package com.halohub.frankenstein.payment;

import com.halohub.frankenstein.common.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentCreateResult {

    private PaymentStatus status;
    private String thirdPartyNo;
    private String clientPayload;
}
