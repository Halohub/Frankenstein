package com.halohub.frankenstein.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.halohub.frankenstein.common.constant.AuthConstants;
import com.halohub.frankenstein.common.enums.CommonErrorCode;
import com.halohub.frankenstein.common.enums.OrderSource;
import com.halohub.frankenstein.common.enums.OrderStatus;
import com.halohub.frankenstein.common.enums.PaymentStatus;
import com.halohub.frankenstein.common.enums.ProductStatus;
import com.halohub.frankenstein.common.exception.BusinessException;
import com.halohub.frankenstein.common.properties.OrderProperties;
import com.halohub.frankenstein.dto.OrderCreateRequest;
import com.halohub.frankenstein.dto.OrderDirectItemRequest;
import com.halohub.frankenstein.entity.BizCartItem;
import com.halohub.frankenstein.entity.BizOrder;
import com.halohub.frankenstein.entity.BizOrderItem;
import com.halohub.frankenstein.entity.BizPayment;
import com.halohub.frankenstein.entity.BizSku;
import com.halohub.frankenstein.entity.BizSpu;
import com.halohub.frankenstein.mapper.BizCartItemMapper;
import com.halohub.frankenstein.mapper.BizOrderItemMapper;
import com.halohub.frankenstein.mapper.BizOrderMapper;
import com.halohub.frankenstein.mapper.BizPaymentMapper;
import com.halohub.frankenstein.mapper.BizSkuMapper;
import com.halohub.frankenstein.mapper.BizSpuMapper;
import com.halohub.frankenstein.satoken.StpMemberUtil;
import com.halohub.frankenstein.service.model.OrderLine;
import com.halohub.frankenstein.vo.OrderDetailVO;
import com.halohub.frankenstein.vo.OrderItemVO;
import com.halohub.frankenstein.vo.OrderPreviewVO;
import com.halohub.frankenstein.vo.OrderVO;
import com.halohub.frankenstein.vo.PageResult;
import com.halohub.frankenstein.vo.PaymentVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class MemberOrderService {

    private final BizOrderMapper bizOrderMapper;
    private final BizOrderItemMapper bizOrderItemMapper;
    private final BizCartItemMapper bizCartItemMapper;
    private final BizSkuMapper bizSkuMapper;
    private final BizSpuMapper bizSpuMapper;
    private final BizPaymentMapper bizPaymentMapper;
    private final OrderProperties orderProperties;

    public MemberOrderService(BizOrderMapper bizOrderMapper,
                              BizOrderItemMapper bizOrderItemMapper,
                              BizCartItemMapper bizCartItemMapper,
                              BizSkuMapper bizSkuMapper,
                              BizSpuMapper bizSpuMapper,
                              BizPaymentMapper bizPaymentMapper,
                              OrderProperties orderProperties) {
        this.bizOrderMapper = bizOrderMapper;
        this.bizOrderItemMapper = bizOrderItemMapper;
        this.bizCartItemMapper = bizCartItemMapper;
        this.bizSkuMapper = bizSkuMapper;
        this.bizSpuMapper = bizSpuMapper;
        this.bizPaymentMapper = bizPaymentMapper;
        this.orderProperties = orderProperties;
    }

    public OrderPreviewVO previewOrder(OrderCreateRequest request) {
        expirePendingOrders(currentMemberId());
        List<OrderLine> lines = resolveOrderLines(currentMemberId(), request);
        return buildPreview(lines);
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderDetailVO createOrder(OrderCreateRequest request) {
        Long memberId = currentMemberId();
        expirePendingOrders(memberId);
        OrderSource source = parseSource(request);
        List<OrderLine> lines = resolveOrderLines(memberId, request);

        BigDecimal totalAmount = sumSubtotal(lines);
        BigDecimal freightAmount = orderProperties.getDefaultFreightAmount();
        BigDecimal payAmount = totalAmount.add(freightAmount);
        LocalDateTime now = LocalDateTime.now();

        BizOrder order = new BizOrder();
        order.setOrderNo(generateOrderNo());
        order.setMemberId(memberId);
        order.setStatus(OrderStatus.PENDING_PAY.getCode());
        order.setSource(source.getCode());
        order.setTotalAmount(totalAmount);
        order.setFreightAmount(freightAmount);
        order.setPayAmount(payAmount);
        order.setCurrency(orderProperties.getDefaultCurrency());
        order.setReceiverName(request.getReceiverName().trim());
        order.setReceiverPhone(request.getReceiverPhone().trim());
        order.setReceiverAddress(request.getReceiverAddress().trim());
        order.setRemark(blankToNull(request.getRemark()));
        order.setExpireTime(now.plusMinutes(orderProperties.getPayTimeoutMinutes()));
        bizOrderMapper.insert(order);

        for (OrderLine line : lines) {
            deductStock(line.getSkuId(), line.getQuantity());
            BizOrderItem item = new BizOrderItem();
            item.setOrderId(order.getId());
            item.setSpuId(line.getSpuId());
            item.setSkuId(line.getSkuId());
            item.setProductTitle(line.getProductTitle());
            item.setSkuCode(line.getSkuCode());
            item.setSkuSpec(line.getSkuSpec());
            item.setUnitPrice(line.getUnitPrice());
            item.setQuantity(line.getQuantity());
            item.setSubtotal(line.getSubtotal());
            bizOrderItemMapper.insert(item);
        }

        if (source == OrderSource.CART) {
            for (OrderLine line : lines) {
                if (line.getCartItemId() != null) {
                    bizCartItemMapper.deleteById(line.getCartItemId());
                }
            }
        }

        return getOrderDetail(order.getId());
    }

    public PageResult<OrderVO> pageOrders(int pageNum, int pageSize, String status) {
        Long memberId = currentMemberId();
        expirePendingOrders(memberId);
        Page<BizOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BizOrder> wrapper = new LambdaQueryWrapper<BizOrder>()
                .eq(BizOrder::getMemberId, memberId)
                .eq(StringUtils.hasText(status), BizOrder::getStatus, status)
                .orderByDesc(BizOrder::getId);
        Page<BizOrder> result = bizOrderMapper.selectPage(page, wrapper);
        return PageResult.of(result.getRecords().stream().map(this::toOrderVO).toList(), result.getTotal());
    }

    public OrderDetailVO getOrderDetail(Long orderId) {
        Long memberId = currentMemberId();
        expirePendingOrders(memberId);
        BizOrder order = getOwnedOrder(memberId, orderId);
        return toOrderDetail(order);
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderDetailVO cancelOrder(Long orderId) {
        Long memberId = currentMemberId();
        BizOrder order = getOwnedOrder(memberId, orderId);
        if (!OrderStatus.PENDING_PAY.getCode().equals(order.getStatus())) {
            throw new BusinessException(CommonErrorCode.OPERATION_FAILED);
        }
        cancelPendingOrder(order);
        return getOrderDetail(orderId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void expirePendingOrders(Long memberId) {
        List<BizOrder> expiredOrders = bizOrderMapper.listExpiredPendingOrders(memberId, LocalDateTime.now());
        for (BizOrder order : expiredOrders) {
            cancelPendingOrder(order);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void expireAllPendingOrders() {
        List<BizOrder> expiredOrders = bizOrderMapper.listAllExpiredPendingOrders(LocalDateTime.now());
        for (BizOrder order : expiredOrders) {
            cancelPendingOrder(order);
        }
    }

    public BizOrder getOwnedPendingOrder(Long memberId, Long orderId) {
        BizOrder order = getOwnedOrder(memberId, orderId);
        if (!OrderStatus.PENDING_PAY.getCode().equals(order.getStatus())) {
            throw new BusinessException(CommonErrorCode.OPERATION_FAILED);
        }
        if (order.getExpireTime() != null && order.getExpireTime().isBefore(LocalDateTime.now())) {
            cancelPendingOrder(order);
            throw new BusinessException(CommonErrorCode.OPERATION_FAILED);
        }
        return order;
    }

    @Transactional(rollbackFor = Exception.class)
    public void markOrderPaid(BizOrder order, LocalDateTime payTime) {
        order.setStatus(OrderStatus.PAID.getCode());
        order.setPayTime(payTime);
        bizOrderMapper.updateById(order);
    }

    private void cancelPendingOrder(BizOrder order) {
        if (!OrderStatus.PENDING_PAY.getCode().equals(order.getStatus())) {
            return;
        }
        List<BizOrderItem> items = bizOrderItemMapper.listByOrderId(order.getId());
        for (BizOrderItem item : items) {
            bizSkuMapper.restoreStock(item.getSkuId(), item.getQuantity());
        }
        order.setStatus(OrderStatus.CANCELLED.getCode());
        order.setCancelTime(LocalDateTime.now());
        bizOrderMapper.updateById(order);

        BizPayment pendingPayment = bizPaymentMapper.findPendingByOrderId(order.getId());
        if (pendingPayment != null) {
            pendingPayment.setStatus(PaymentStatus.CLOSED.getCode());
            bizPaymentMapper.updateById(pendingPayment);
        }
    }

    private List<OrderLine> resolveOrderLines(Long memberId, OrderCreateRequest request) {
        OrderSource source = parseSource(request);
        return switch (source) {
            case CART -> resolveCartLines(memberId, request.getCartItemIds());
            case DIRECT -> resolveDirectLines(request.getItems());
        };
    }

    private List<OrderLine> resolveCartLines(Long memberId, List<Long> cartItemIds) {
        if (cartItemIds == null || cartItemIds.isEmpty()) {
            throw new BusinessException(CommonErrorCode.PARAM_VALIDATION_FAILED);
        }
        Set<Long> uniqueIds = new HashSet<>(cartItemIds);
        if (uniqueIds.size() != cartItemIds.size()) {
            throw new BusinessException(CommonErrorCode.PARAM_VALIDATION_FAILED);
        }
        List<OrderLine> lines = new ArrayList<>();
        for (Long cartItemId : cartItemIds) {
            BizCartItem cartItem = bizCartItemMapper.selectById(cartItemId);
            if (cartItem == null || !memberId.equals(cartItem.getMemberId())) {
                throw new BusinessException(CommonErrorCode.NO_CORRESPONDING_DATA);
            }
            lines.add(buildLineFromSku(cartItem.getSkuId(), cartItem.getQuantity(), cartItemId));
        }
        return lines;
    }

    private List<OrderLine> resolveDirectLines(List<OrderDirectItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new BusinessException(CommonErrorCode.PARAM_VALIDATION_FAILED);
        }
        java.util.Map<Long, Integer> skuQuantities = new java.util.LinkedHashMap<>();
        for (OrderDirectItemRequest item : items) {
            skuQuantities.merge(item.getSkuId(), item.getQuantity(), Integer::sum);
        }
        List<OrderLine> lines = new ArrayList<>();
        for (java.util.Map.Entry<Long, Integer> entry : skuQuantities.entrySet()) {
            lines.add(buildLineFromSku(entry.getKey(), entry.getValue(), null));
        }
        return lines;
    }

    private OrderLine buildLineFromSku(Long skuId, int quantity, Long cartItemId) {
        BizSku sku = bizSkuMapper.selectById(skuId);
        BizSpu spu = sku == null ? null : bizSpuMapper.selectById(sku.getSpuId());
        validatePurchasable(sku, spu, quantity);
        BigDecimal subtotal = sku.getPrice().multiply(BigDecimal.valueOf(quantity));
        return OrderLine.builder()
                .cartItemId(cartItemId)
                .skuId(sku.getId())
                .spuId(spu.getId())
                .quantity(quantity)
                .unitPrice(sku.getPrice())
                .productTitle(spu.getTitle())
                .skuCode(sku.getSkuCode())
                .skuSpec(sku.getSpecJson())
                .subtotal(subtotal)
                .build();
    }

    private void validatePurchasable(BizSku sku, BizSpu spu, int quantity) {
        if (sku == null || sku.getStatus() == null || sku.getStatus() != AuthConstants.STATUS_ACTIVE) {
            throw new BusinessException(CommonErrorCode.NO_CORRESPONDING_DATA);
        }
        if (spu == null || spu.getStatus() == null || spu.getStatus() != ProductStatus.ON.getCode()) {
            throw new BusinessException(CommonErrorCode.NO_CORRESPONDING_DATA);
        }
        if (sku.getStock() == null || quantity > sku.getStock()) {
            throw new BusinessException(CommonErrorCode.INSUFFICIENT_STOCK);
        }
    }

    private void deductStock(Long skuId, int quantity) {
        int updated = bizSkuMapper.deductStock(skuId, quantity);
        if (updated == 0) {
            throw new BusinessException(CommonErrorCode.INSUFFICIENT_STOCK);
        }
    }

    private OrderPreviewVO buildPreview(List<OrderLine> lines) {
        BigDecimal totalAmount = sumSubtotal(lines);
        BigDecimal freightAmount = orderProperties.getDefaultFreightAmount();
        OrderPreviewVO preview = new OrderPreviewVO();
        preview.setItems(lines.stream().map(this::toOrderItemVO).toList());
        preview.setTotalAmount(totalAmount);
        preview.setFreightAmount(freightAmount);
        preview.setPayAmount(totalAmount.add(freightAmount));
        preview.setCurrency(orderProperties.getDefaultCurrency());
        return preview;
    }

    private BigDecimal sumSubtotal(List<OrderLine> lines) {
        return lines.stream()
                .map(OrderLine::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private OrderSource parseSource(OrderCreateRequest request) {
        OrderSource source = OrderSource.fromCode(request.getSource());
        if (source == null) {
            throw new BusinessException(CommonErrorCode.PARAM_VALIDATION_FAILED);
        }
        return source;
    }

    private BizOrder getOwnedOrder(Long memberId, Long orderId) {
        BizOrder order = bizOrderMapper.selectById(orderId);
        if (order == null || !memberId.equals(order.getMemberId())) {
            throw new BusinessException(CommonErrorCode.NO_CORRESPONDING_DATA);
        }
        return order;
    }

    private OrderDetailVO toOrderDetail(BizOrder order) {
        OrderDetailVO detail = new OrderDetailVO();
        BeanUtils.copyProperties(toOrderVO(order), detail);
        detail.setItems(bizOrderItemMapper.listByOrderId(order.getId()).stream()
                .map(this::toOrderItemVO)
                .toList());
        BizPayment payment = bizPaymentMapper.findPendingByOrderId(order.getId());
        if (payment == null) {
            LambdaQueryWrapper<BizPayment> wrapper = new LambdaQueryWrapper<BizPayment>()
                    .eq(BizPayment::getOrderId, order.getId())
                    .eq(BizPayment::getStatus, PaymentStatus.SUCCESS.getCode())
                    .orderByDesc(BizPayment::getId)
                    .last("LIMIT 1");
            payment = bizPaymentMapper.selectOne(wrapper);
        }
        if (payment != null) {
            detail.setPayment(toPaymentVO(payment));
        }
        return detail;
    }

    private OrderVO toOrderVO(BizOrder order) {
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        return vo;
    }

    private OrderItemVO toOrderItemVO(BizOrderItem item) {
        OrderItemVO vo = new OrderItemVO();
        BeanUtils.copyProperties(item, vo);
        return vo;
    }

    private OrderItemVO toOrderItemVO(OrderLine line) {
        OrderItemVO vo = new OrderItemVO();
        vo.setSpuId(line.getSpuId());
        vo.setSkuId(line.getSkuId());
        vo.setProductTitle(line.getProductTitle());
        vo.setSkuCode(line.getSkuCode());
        vo.setSkuSpec(line.getSkuSpec());
        vo.setUnitPrice(line.getUnitPrice());
        vo.setQuantity(line.getQuantity());
        vo.setSubtotal(line.getSubtotal());
        return vo;
    }

    private PaymentVO toPaymentVO(BizPayment payment) {
        PaymentVO vo = new PaymentVO();
        BeanUtils.copyProperties(payment, vo);
        return vo;
    }

    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(1000, 9999);
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private Long currentMemberId() {
        return StpMemberUtil.getLoginIdAsLong();
    }
}
