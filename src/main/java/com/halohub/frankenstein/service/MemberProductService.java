package com.halohub.frankenstein.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.halohub.frankenstein.common.constant.AuthConstants;
import com.halohub.frankenstein.common.enums.ProductStatus;
import com.halohub.frankenstein.common.enums.CommonErrorCode;
import com.halohub.frankenstein.common.exception.BusinessException;
import com.halohub.frankenstein.entity.BizCategory;
import com.halohub.frankenstein.entity.BizSku;
import com.halohub.frankenstein.entity.BizSpu;
import com.halohub.frankenstein.mapper.BizSkuMapper;
import com.halohub.frankenstein.mapper.BizSpuMapper;
import com.halohub.frankenstein.vo.CategoryTreeVO;
import com.halohub.frankenstein.vo.PageResult;
import com.halohub.frankenstein.vo.SkuVO;
import com.halohub.frankenstein.vo.SpuDetailVO;
import com.halohub.frankenstein.vo.SpuVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MemberProductService {

    private final BizSpuMapper bizSpuMapper;
    private final BizSkuMapper bizSkuMapper;
    private final AdminCategoryService adminCategoryService;

    public MemberProductService(BizSpuMapper bizSpuMapper,
                                BizSkuMapper bizSkuMapper,
                                AdminCategoryService adminCategoryService) {
        this.bizSpuMapper = bizSpuMapper;
        this.bizSkuMapper = bizSkuMapper;
        this.adminCategoryService = adminCategoryService;
    }

    public List<CategoryTreeVO> listActiveCategoryTree() {
        return adminCategoryService.listActiveTree();
    }

    public PageResult<SpuVO> pageOnShelfSpu(int pageNum, int pageSize, Long categoryId, String keyword) {
        Page<BizSpu> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BizSpu> wrapper = new LambdaQueryWrapper<BizSpu>()
                .eq(BizSpu::getStatus, ProductStatus.ON.getCode())
                .eq(categoryId != null, BizSpu::getCategoryId, categoryId)
                .like(StringUtils.hasText(keyword), BizSpu::getTitle, keyword)
                .orderByDesc(BizSpu::getId);
        Page<BizSpu> result = bizSpuMapper.selectPage(page, wrapper);
        Map<Long, String> categoryNames = loadActiveCategoryNames(result.getRecords());
        List<SpuVO> list = result.getRecords().stream()
                .filter(spu -> categoryNames.containsKey(spu.getCategoryId()))
                .map(spu -> toSpuVO(spu, categoryNames.get(spu.getCategoryId())))
                .toList();
        return PageResult.of(list, result.getTotal());
    }

    public SpuDetailVO getOnShelfSpuDetail(Long spuId) {
        BizSpu spu = getOnShelfSpuOrThrow(spuId);
        BizCategory category = adminCategoryService.getCategoryOrThrow(spu.getCategoryId());
        ensureCategoryActive(category);

        SpuDetailVO detail = new SpuDetailVO();
        BeanUtils.copyProperties(toSpuVO(spu, category.getName()), detail);
        detail.setSkus(bizSkuMapper.listBySpuId(spuId).stream()
                .filter(sku -> sku.getStatus() != null && sku.getStatus() == AuthConstants.STATUS_ACTIVE)
                .map(this::toSkuVO)
                .toList());
        if (detail.getSkus().isEmpty()) {
            throw new BusinessException(CommonErrorCode.NO_CORRESPONDING_DATA);
        }
        return detail;
    }

    public SkuVO getActiveSkuDetail(Long skuId) {
        BizSku sku = bizSkuMapper.selectById(skuId);
        if (sku == null || sku.getStatus() == null || sku.getStatus() != AuthConstants.STATUS_ACTIVE) {
            throw new BusinessException(CommonErrorCode.NO_CORRESPONDING_DATA);
        }
        getOnShelfSpuOrThrow(sku.getSpuId());
        return toSkuVO(sku);
    }

    private BizSpu getOnShelfSpuOrThrow(Long spuId) {
        BizSpu spu = bizSpuMapper.selectById(spuId);
        if (spu == null || spu.getStatus() == null || spu.getStatus() != ProductStatus.ON.getCode()) {
            throw new BusinessException(CommonErrorCode.NO_CORRESPONDING_DATA);
        }
        ensureCategoryActive(adminCategoryService.getCategoryOrThrow(spu.getCategoryId()));
        return spu;
    }

    private void ensureCategoryActive(BizCategory category) {
        if (category.getStatus() == null || category.getStatus() != AuthConstants.STATUS_ACTIVE) {
            throw new BusinessException(CommonErrorCode.NO_CORRESPONDING_DATA);
        }
    }

    private Map<Long, String> loadActiveCategoryNames(List<BizSpu> spus) {
        Set<Long> categoryIds = spus.stream().map(BizSpu::getCategoryId).collect(Collectors.toSet());
        if (categoryIds.isEmpty()) {
            return Map.of();
        }
        return categoryIds.stream()
                .map(adminCategoryService::getCategoryOrThrow)
                .filter(category -> category.getStatus() != null
                        && category.getStatus() == AuthConstants.STATUS_ACTIVE)
                .collect(Collectors.toMap(BizCategory::getId, BizCategory::getName));
    }

    private SpuVO toSpuVO(BizSpu spu, String categoryName) {
        SpuVO vo = new SpuVO();
        BeanUtils.copyProperties(spu, vo);
        vo.setCategoryName(categoryName);
        return vo;
    }

    private SkuVO toSkuVO(BizSku sku) {
        SkuVO vo = new SkuVO();
        BeanUtils.copyProperties(sku, vo);
        return vo;
    }
}
