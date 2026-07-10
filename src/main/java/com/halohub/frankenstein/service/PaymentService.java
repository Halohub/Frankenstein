package com.halohub.frankenstein.service;

import com.halohub.frankenstein.common.enums.CommonErrorCode;
import com.halohub.frankenstein.common.enums.PaymentChannel;
import com.halohub.frankenstein.common.enums.PaymentStatus;
import com.halohub.frankenstein.common.exception.BusinessException;
import com.halohub.frankenstein.common.properties.OrderProperties;
import com.halohub.frankenstein.dto.PaymentCallbackRequest;
import com.halohub.frankenstein.dto.PaymentCreateRequest;
import com.halohub.frankenstein.entity.BizOrder;
import com.halohub.frankenstein.entity.BizPayment;
import com.halohub.frankenstein.mapper.BizPaymentMapper;
import com.halohub.frankenstein.payment.PaymentCallbackResult;
import com.halohub.frankenstein.payment.PaymentContext;
import com.halohub.frankenstein.payment.PaymentCreateResult;
import com.halohub.frankenstein.payment.PaymentProvider;
import com.halohub.frankenstein.payment.PaymentProviderFactory;
import com.halohub.frankenstein.satoken.StpMemberUtil;
import com.halohub.frankenstein.vo.PaymentVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PaymentService {

    private final BizPaymentMapper bizPaymentMapper;
    private final MemberOrderService memberOrderService;
    private final PaymentProviderFactory paymentProviderFactory;
    private final OrderProperties orderProperties;

    public PaymentService(BizPaymentMapper bizPaymentMapper,
                          MemberOrderService memberOrderService,
                          PaymentProviderFactory paymentProviderFactory,
                          OrderProperties orderProperties) {
        this.bizPaymentMapper = bizPaymentMapper;
        this.memberOrderService = memberOrderService;
        this.paymentProviderFactory = paymentProviderFactory;
        this.orderProperties = orderProperties;
    }

    @Transactional(rollbackFor = Exception.class)
    public PaymentVO createPayment(PaymentCreateRequest request) {
        Long memberId = StpMemberUtil.getLoginIdAsLong();
        memberOrderService.expirePendingOrders(memberId);

        PaymentChannel channel = PaymentChannel.fromCode(request.getChannel());
        if (channel == null) {
            throw new BusinessException(CommonErrorCode.PARAM_VALIDATION_FAILED);
        }

        BizOrder order = memberOrderService.getOwnedPendingOrder(memberId, request.getOrderId());
        closePendingPayment(order.getId());

        LocalDateTime now = LocalDateTime.now();
        BizPayment payment = new BizPayment();
        payment.setPaymentNo(generatePaymentNo());
        payment.setOrderId(order.getId());
        payment.setOrderNo(order.getOrderNo());
        payment.setMemberId(memberId);
        payment.setChannel(channel.getCode());
        payment.setAmount(order.getPayAmount());
        payment.setCurrency(order.getCurrency());
        payment.setStatus(PaymentStatus.PENDING.getCode());
        payment.setExpireTime(now.plusMinutes(orderProperties.getPayTimeoutMinutes()));
        bizPaymentMapper.insert(payment);

        PaymentProvider provider = paymentProviderFactory.get(channel);
        PaymentCreateResult createResult = provider.create(PaymentContext.builder()
                .payment(payment)
                .orderNo(order.getOrderNo())
                .memberId(memberId)
                .build());
        if (createResult.getClientPayload() != null) {
            payment.setClientPayload(createResult.getClientPayload());
        }
        if (createResult.getThirdPartyNo() != null) {
            payment.setThirdPartyNo(createResult.getThirdPartyNo());
        }
        bizPaymentMapper.updateById(payment);
        return toPaymentVO(payment);
    }

    public PaymentVO getPaymentStatus(String paymentNo) {
        Long memberId = StpMemberUtil.getLoginIdAsLong();
        BizPayment payment = getOwnedPayment(memberId, paymentNo);
        return toPaymentVO(payment);
    }

    @Transactional(rollbackFor = Exception.class)
    public PaymentVO handleCallback(PaymentChannel channel, PaymentCallbackRequest request) {
        PaymentProvider provider = paymentProviderFactory.get(channel);
        PaymentCallbackResult callbackResult = provider.handleCallback(request);
        if (!callbackResult.isHandled()) {
            throw new BusinessException(CommonErrorCode.OPERATION_FAILED);
        }
        completePayment(request.getPaymentNo(), callbackResult.getThirdPartyNo(),
                callbackResult.getCallbackRaw(), callbackResult.getStatus());
        BizPayment payment = bizPaymentMapper.findByPaymentNo(request.getPaymentNo());
        return toPaymentVO(payment);
    }

    @Transactional(rollbackFor = Exception.class)
    public void completePayment(String paymentNo,
                                String thirdPartyNo,
                                String callbackRaw,
                                PaymentStatus targetStatus) {
        BizPayment payment = bizPaymentMapper.findByPaymentNo(paymentNo);
        if (payment == null) {
            throw new BusinessException(CommonErrorCode.NO_CORRESPONDING_DATA);
        }
        if (PaymentStatus.SUCCESS.getCode().equals(payment.getStatus())) {
            return;
        }
        if (!PaymentStatus.PENDING.getCode().equals(payment.getStatus())) {
            throw new BusinessException(CommonErrorCode.OPERATION_FAILED);
        }

        LocalDateTime now = LocalDateTime.now();
        if (targetStatus == PaymentStatus.SUCCESS) {
            payment.setStatus(PaymentStatus.SUCCESS.getCode());
            payment.setThirdPartyNo(thirdPartyNo);
            payment.setCallbackRaw(callbackRaw);
            payment.setPayTime(now);
            bizPaymentMapper.updateById(payment);

            BizOrder order = memberOrderService.getOwnedPendingOrder(payment.getMemberId(), payment.getOrderId());
            memberOrderService.markOrderPaid(order, now);
            return;
        }

        payment.setStatus(targetStatus.getCode());
        payment.setCallbackRaw(callbackRaw);
        bizPaymentMapper.updateById(payment);
    }

    private void closePendingPayment(Long orderId) {
        BizPayment pendingPayment = bizPaymentMapper.findPendingByOrderId(orderId);
        if (pendingPayment != null) {
            pendingPayment.setStatus(PaymentStatus.CLOSED.getCode());
            bizPaymentMapper.updateById(pendingPayment);
        }
    }

    private BizPayment getOwnedPayment(Long memberId, String paymentNo) {
        BizPayment payment = bizPaymentMapper.findByPaymentNo(paymentNo);
        if (payment == null || !memberId.equals(payment.getMemberId())) {
            throw new BusinessException(CommonErrorCode.NO_CORRESPONDING_DATA);
        }
        return payment;
    }

    private PaymentVO toPaymentVO(BizPayment payment) {
        PaymentVO vo = new PaymentVO();
        BeanUtils.copyProperties(payment, vo);
        return vo;
    }

    private String generatePaymentNo() {
        return "PAY" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(1000, 9999);
    }
}
