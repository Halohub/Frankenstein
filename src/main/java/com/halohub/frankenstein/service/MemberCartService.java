package com.halohub.frankenstein.service;

import com.halohub.frankenstein.common.constant.AuthConstants;
import com.halohub.frankenstein.common.enums.CommonErrorCode;
import com.halohub.frankenstein.common.enums.ProductStatus;
import com.halohub.frankenstein.common.exception.BusinessException;
import com.halohub.frankenstein.dto.CartAddRequest;
import com.halohub.frankenstein.dto.CartSelectRequest;
import com.halohub.frankenstein.dto.CartUpdateRequest;
import com.halohub.frankenstein.entity.BizCartItem;
import com.halohub.frankenstein.entity.BizSku;
import com.halohub.frankenstein.entity.BizSpu;
import com.halohub.frankenstein.mapper.BizCartItemMapper;
import com.halohub.frankenstein.mapper.BizSkuMapper;
import com.halohub.frankenstein.mapper.BizSpuMapper;
import com.halohub.frankenstein.satoken.StpMemberUtil;
import com.halohub.frankenstein.vo.CartItemVO;
import com.halohub.frankenstein.vo.CartListVO;
import com.halohub.frankenstein.vo.SkuVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class MemberCartService {

    private final BizCartItemMapper bizCartItemMapper;
    private final BizSkuMapper bizSkuMapper;
    private final BizSpuMapper bizSpuMapper;
    private final MemberProductService memberProductService;

    public MemberCartService(BizCartItemMapper bizCartItemMapper,
                             BizSkuMapper bizSkuMapper,
                             BizSpuMapper bizSpuMapper,
                             MemberProductService memberProductService) {
        this.bizCartItemMapper = bizCartItemMapper;
        this.bizSkuMapper = bizSkuMapper;
        this.bizSpuMapper = bizSpuMapper;
        this.memberProductService = memberProductService;
    }

    public CartListVO listCart() {
        Long memberId = currentMemberId();
        List<BizCartItem> cartItems = bizCartItemMapper.listByMemberId(memberId);
        List<CartItemVO> items = cartItems.stream().map(this::toCartItemVO).toList();
        return buildCartList(items);
    }

    @Transactional(rollbackFor = Exception.class)
    public CartListVO addItem(CartAddRequest request) {
        Long memberId = currentMemberId();
        SkuVO sku = memberProductService.getActiveSkuDetail(request.getSkuId());
        ensureStock(sku.getStock(), request.getQuantity());

        BizCartItem existing = bizCartItemMapper.findByMemberAndSku(memberId, request.getSkuId());
        if (existing != null) {
            int newQuantity = existing.getQuantity() + request.getQuantity();
            ensureStock(sku.getStock(), newQuantity);
            existing.setQuantity(newQuantity);
            existing.setSelected(AuthConstants.STATUS_ACTIVE);
            bizCartItemMapper.updateById(existing);
        } else {
            BizCartItem cartItem = new BizCartItem();
            cartItem.setMemberId(memberId);
            cartItem.setSkuId(request.getSkuId());
            cartItem.setQuantity(request.getQuantity());
            cartItem.setSelected(AuthConstants.STATUS_ACTIVE);
            bizCartItemMapper.insert(cartItem);
        }
        return listCart();
    }

    @Transactional(rollbackFor = Exception.class)
    public CartListVO updateItem(Long itemId, CartUpdateRequest request) {
        Long memberId = currentMemberId();
        BizCartItem cartItem = getOwnedCartItem(memberId, itemId);
        SkuVO sku = memberProductService.getActiveSkuDetail(cartItem.getSkuId());
        ensureStock(sku.getStock(), request.getQuantity());
        cartItem.setQuantity(request.getQuantity());
        bizCartItemMapper.updateById(cartItem);
        return listCart();
    }

    @Transactional(rollbackFor = Exception.class)
    public CartListVO removeItem(Long itemId) {
        Long memberId = currentMemberId();
        BizCartItem cartItem = getOwnedCartItem(memberId, itemId);
        bizCartItemMapper.deleteById(cartItem.getId());
        return listCart();
    }

    @Transactional(rollbackFor = Exception.class)
    public CartListVO updateSelect(CartSelectRequest request) {
        Long memberId = currentMemberId();
        Set<Long> itemIds = new HashSet<>(request.getItemIds());
        int selected = Boolean.TRUE.equals(request.getSelected())
                ? AuthConstants.STATUS_ACTIVE
                : AuthConstants.STATUS_DISABLED;
        List<BizCartItem> cartItems = bizCartItemMapper.listByMemberId(memberId);
        for (BizCartItem cartItem : cartItems) {
            if (itemIds.contains(cartItem.getId())) {
                cartItem.setSelected(selected);
                bizCartItemMapper.updateById(cartItem);
            }
        }
        return listCart();
    }

    private BizCartItem getOwnedCartItem(Long memberId, Long itemId) {
        BizCartItem cartItem = bizCartItemMapper.selectById(itemId);
        if (cartItem == null || !memberId.equals(cartItem.getMemberId())) {
            throw new BusinessException(CommonErrorCode.NO_CORRESPONDING_DATA);
        }
        return cartItem;
    }

    private CartItemVO toCartItemVO(BizCartItem cartItem) {
        CartItemVO vo = new CartItemVO();
        vo.setId(cartItem.getId());
        vo.setSkuId(cartItem.getSkuId());
        vo.setQuantity(cartItem.getQuantity());
        vo.setSelected(cartItem.getSelected() != null && cartItem.getSelected() == AuthConstants.STATUS_ACTIVE);

        BizSku sku = bizSkuMapper.selectById(cartItem.getSkuId());
        if (sku == null) {
            vo.setValid(false);
            return vo;
        }
        BizSpu spu = bizSpuMapper.selectById(sku.getSpuId());
        boolean valid = isPurchasable(sku, spu);
        vo.setValid(valid);
        vo.setSpuId(sku.getSpuId());
        vo.setSkuCode(sku.getSkuCode());
        vo.setSpecJson(sku.getSpecJson());
        vo.setPrice(sku.getPrice());
        vo.setStock(sku.getStock());
        vo.setSkuImage(sku.getImage());
        if (spu != null) {
            vo.setSpuTitle(spu.getTitle());
            vo.setMainImage(spu.getMainImage());
        }
        if (sku.getPrice() != null) {
            vo.setSubtotal(sku.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }
        if (!valid) {
            vo.setSelected(false);
        } else if (sku.getStock() != null && cartItem.getQuantity() > sku.getStock()) {
            vo.setValid(false);
        }
        return vo;
    }

    private boolean isPurchasable(BizSku sku, BizSpu spu) {
        if (sku.getStatus() == null || sku.getStatus() != AuthConstants.STATUS_ACTIVE) {
            return false;
        }
        if (spu == null || spu.getStatus() == null || spu.getStatus() != ProductStatus.ON.getCode()) {
            return false;
        }
        return sku.getStock() != null && sku.getStock() > 0;
    }

    private CartListVO buildCartList(List<CartItemVO> items) {
        CartListVO listVO = new CartListVO();
        listVO.setItems(items);
        listVO.setTotalCount(items.size());
        int selectedCount = 0;
        BigDecimal selectedAmount = BigDecimal.ZERO;
        for (CartItemVO item : items) {
            if (Boolean.TRUE.equals(item.getSelected()) && Boolean.TRUE.equals(item.getValid())) {
                selectedCount++;
                if (item.getSubtotal() != null) {
                    selectedAmount = selectedAmount.add(item.getSubtotal());
                }
            }
        }
        listVO.setSelectedCount(selectedCount);
        listVO.setSelectedAmount(selectedAmount);
        return listVO;
    }

    private void ensureStock(Integer stock, int quantity) {
        if (stock == null || quantity > stock) {
            throw new BusinessException(CommonErrorCode.INSUFFICIENT_STOCK);
        }
    }

    private Long currentMemberId() {
        return StpMemberUtil.getLoginIdAsLong();
    }
}
