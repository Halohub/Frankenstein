package com.halohub.frankenstein.payment;

import com.halohub.frankenstein.entity.BizPayment;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentContext {

    private BizPayment payment;
    private String orderNo;
    private Long memberId;
}
