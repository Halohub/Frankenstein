package com.halohub.frankenstein.schedule;

import com.halohub.frankenstein.service.MemberOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderExpireScheduler {

    private final MemberOrderService memberOrderService;

    public OrderExpireScheduler(MemberOrderService memberOrderService) {
        this.memberOrderService = memberOrderService;
    }

    @Scheduled(cron = "0 */1 * * * ?")
    public void expirePendingOrders() {
        memberOrderService.expireAllPendingOrders();
    }
}
