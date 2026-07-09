package com.halohub.frankenstein.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.halohub.frankenstein.common.constant.AuthConstants;
import com.halohub.frankenstein.common.enums.CommonErrorCode;
import com.halohub.frankenstein.common.enums.ProductStatus;
import com.halohub.frankenstein.common.exception.BusinessException;
import com.halohub.frankenstein.dto.SkuItemRequest;
import com.halohub.frankenstein.dto.SpuCreateRequest;
import com.halohub.frankenstein.dto.SpuUpdateRequest;
import com.halohub.frankenstein.entity.BizCategory;
import com.halohub.frankenstein.entity.BizSku;
import com.halohub.frankenstein.entity.BizSpu;
import com.halohub.frankenstein.mapper.BizSkuMapper;
import com.halohub.frankenstein.mapper.BizSpuMapper;
import com.halohub.frankenstein.vo.PageResult;
import com.halohub.frankenstein.vo.SkuVO;
import com.halohub.frankenstein.vo.SpuDetailVO;
import com.halohub.frankenstein.vo.SpuVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminProductService {

    private final BizSpuMapper bizSpuMapper;
    private final BizSkuMapper bizSkuMapper;
    private final AdminCategoryService adminCategoryService;

    public AdminProductService(BizSpuMapper bizSpuMapper,
                               BizSkuMapper bizSkuMapper,
                               AdminCategoryService adminCategoryService) {
        this.bizSpuMapper = bizSpuMapper;
        this.bizSkuMapper = bizSkuMapper;
        this.adminCategoryService = adminCategoryService;
    }

    public PageResult<SpuVO> pageSpu(int pageNum, int pageSize, Long categoryId, String title, Integer status) {
        Page<BizSpu> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BizSpu> wrapper = new LambdaQueryWrapper<BizSpu>()
                .eq(categoryId != null, BizSpu::getCategoryId, categoryId)
                .like(StringUtils.hasText(title), BizSpu::getTitle, title)
                .eq(status != null, BizSpu::getStatus, status)
                .orderByDesc(BizSpu::getId);
        Page<BizSpu> result = bizSpuMapper.selectPage(page, wrapper);
        Map<Long, String> categoryNames = loadCategoryNames(result.getRecords());
        List<SpuVO> list = result.getRecords().stream()
                .map(spu -> toSpuVO(spu, categoryNames.get(spu.getCategoryId())))
                .toList();
        return PageResult.of(list, result.getTotal());
    }

    public SpuDetailVO getSpuDetail(Long spuId) {
        BizSpu spu = getSpuOrThrow(spuId);
        BizCategory category = adminCategoryService.getCategoryOrThrow(spu.getCategoryId());
        SpuDetailVO detail = new SpuDetailVO();
        BeanUtils.copyProperties(toSpuVO(spu, category.getName()), detail);
        detail.setSkus(bizSkuMapper.listBySpuId(spuId).stream().map(this::toSkuVO).toList());
        return detail;
    }

    @Transactional(rollbackFor = Exception.class)
    public SpuDetailVO createSpu(SpuCreateRequest request) {
        adminCategoryService.getCategoryOrThrow(request.getCategoryId());
        validateSkuItems(request.getSkus(), null);

        BizSpu spu = new BizSpu();
        spu.setCategoryId(request.getCategoryId());
        spu.setTitle(request.getTitle().trim());
        spu.setSubtitle(blankToNull(request.getSubtitle()));
        spu.setDescription(blankToNull(request.getDescription()));
        spu.setMainImage(blankToNull(request.getMainImage()));
        spu.setStatus(request.getStatus());
        applyPriceRange(spu, request.getSkus());
        bizSpuMapper.insert(spu);

        saveSkus(spu.getId(), request.getSkus());
        return getSpuDetail(spu.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public SpuDetailVO updateSpu(Long spuId, SpuUpdateRequest request) {
        BizSpu spu = getSpuOrThrow(spuId);
        adminCategoryService.getCategoryOrThrow(request.getCategoryId());
        validateSkuItems(request.getSkus(), spuId);

        spu.setCategoryId(request.getCategoryId());
        spu.setTitle(request.getTitle().trim());
        spu.setSubtitle(blankToNull(request.getSubtitle()));
        spu.setDescription(blankToNull(request.getDescription()));
        spu.setMainImage(blankToNull(request.getMainImage()));
        spu.setStatus(request.getStatus());
        applyPriceRange(spu, request.getSkus());
        bizSpuMapper.updateById(spu);

        replaceSkus(spuId, request.getSkus());
        return getSpuDetail(spuId);
    }

    @Transactional(rollbackFor = Exception.class)
    public SpuDetailVO updateSpuStatus(Long spuId, Integer status) {
        BizSpu spu = getSpuOrThrow(spuId);
        if (ProductStatus.fromCode(status) == null) {
            throw new BusinessException(CommonErrorCode.PARAM_VALIDATION_FAILED);
        }
        spu.setStatus(status);
        bizSpuMapper.updateById(spu);
        return getSpuDetail(spuId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteSpu(Long spuId) {
        getSpuOrThrow(spuId);
        bizSpuMapper.deleteById(spuId);
        List<BizSku> skus = bizSkuMapper.listBySpuId(spuId);
        for (BizSku sku : skus) {
            bizSkuMapper.deleteById(sku.getId());
        }
    }

    public BizSpu getSpuOrThrow(Long spuId) {
        BizSpu spu = bizSpuMapper.selectById(spuId);
        if (spu == null) {
            throw new BusinessException(CommonErrorCode.NO_CORRESPONDING_DATA);
        }
        return spu;
    }

    private void validateSkuItems(List<SkuItemRequest> skus, Long spuId) {
        Set<String> skuCodes = new HashSet<>();
        for (SkuItemRequest item : skus) {
            String skuCode = item.getSkuCode().trim();
            if (!skuCodes.add(skuCode)) {
                throw new BusinessException(CommonErrorCode.DUPLICATE_ENTRY);
            }
            if (bizSkuMapper.countBySkuCode(skuCode, item.getId()) > 0) {
                throw new BusinessException(CommonErrorCode.DUPLICATE_ENTRY);
            }
            if (item.getId() != null && spuId != null) {
                BizSku existing = bizSkuMapper.selectById(item.getId());
                if (existing == null || !spuId.equals(existing.getSpuId())) {
                    throw new BusinessException(CommonErrorCode.NO_CORRESPONDING_DATA);
                }
            }
        }
    }

    private void saveSkus(Long spuId, List<SkuItemRequest> skus) {
        for (SkuItemRequest item : skus) {
            BizSku sku = toSkuEntity(spuId, item);
            bizSkuMapper.insert(sku);
        }
    }

    private void replaceSkus(Long spuId, List<SkuItemRequest> skus) {
        List<BizSku> existingSkus = bizSkuMapper.listBySpuId(spuId);
        Set<Long> keepIds = skus.stream()
                .map(SkuItemRequest::getId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        for (BizSku existing : existingSkus) {
            if (!keepIds.contains(existing.getId())) {
                bizSkuMapper.deleteById(existing.getId());
            }
        }

        for (SkuItemRequest item : skus) {
            if (item.getId() == null) {
                bizSkuMapper.insert(toSkuEntity(spuId, item));
            } else {
                BizSku sku = bizSkuMapper.selectById(item.getId());
                if (sku == null || !spuId.equals(sku.getSpuId())) {
                    throw new BusinessException(CommonErrorCode.NO_CORRESPONDING_DATA);
                }
                sku.setSkuCode(item.getSkuCode().trim());
                sku.setSpecJson(blankToNull(item.getSpecJson()));
                sku.setPrice(item.getPrice());
                sku.setStock(item.getStock());
                sku.setImage(blankToNull(item.getImage()));
                sku.setStatus(item.getStatus());
                bizSkuMapper.updateById(sku);
            }
        }
    }

    private BizSku toSkuEntity(Long spuId, SkuItemRequest item) {
        BizSku sku = new BizSku();
        sku.setSpuId(spuId);
        sku.setSkuCode(item.getSkuCode().trim());
        sku.setSpecJson(blankToNull(item.getSpecJson()));
        sku.setPrice(item.getPrice());
        sku.setStock(item.getStock());
        sku.setImage(blankToNull(item.getImage()));
        sku.setStatus(item.getStatus());
        return sku;
    }

    private void applyPriceRange(BizSpu spu, List<SkuItemRequest> skus) {
        BigDecimal min = skus.stream().map(SkuItemRequest::getPrice).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal max = skus.stream().map(SkuItemRequest::getPrice).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        spu.setMinPrice(min);
        spu.setMaxPrice(max);
    }

    private Map<Long, String> loadCategoryNames(List<BizSpu> spus) {
        Set<Long> categoryIds = spus.stream().map(BizSpu::getCategoryId).collect(Collectors.toSet());
        if (categoryIds.isEmpty()) {
            return Map.of();
        }
        return categoryIds.stream()
                .collect(Collectors.toMap(id -> id,
                        id -> adminCategoryService.getCategoryOrThrow(id).getName()));
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

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
