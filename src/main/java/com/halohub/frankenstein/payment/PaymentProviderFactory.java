package com.halohub.frankenstein.payment;

import com.halohub.frankenstein.common.enums.CommonErrorCode;
import com.halohub.frankenstein.common.enums.PaymentChannel;
import com.halohub.frankenstein.common.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PaymentProviderFactory {

    private final Map<PaymentChannel, PaymentProvider> providerMap;

    public PaymentProviderFactory(List<PaymentProvider> providers) {
        Map<PaymentChannel, PaymentProvider> map = new EnumMap<>(PaymentChannel.class);
        for (PaymentProvider provider : providers) {
            map.put(provider.channel(), provider);
        }
        this.providerMap = Map.copyOf(map);
    }

    public PaymentProvider get(PaymentChannel channel) {
        PaymentProvider provider = providerMap.get(channel);
        if (provider == null) {
            throw new BusinessException(CommonErrorCode.PAYMENT_CHANNEL_UNAVAILABLE);
        }
        return provider;
    }
}
