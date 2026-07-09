package com.halohub.frankenstein.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.halohub.frankenstein.common.constant.AuthConstants;
import com.halohub.frankenstein.common.constant.CategoryConstants;
import com.halohub.frankenstein.common.enums.CommonErrorCode;
import com.halohub.frankenstein.common.exception.BusinessException;
import com.halohub.frankenstein.dto.CategoryCreateRequest;
import com.halohub.frankenstein.dto.CategoryUpdateRequest;
import com.halohub.frankenstein.entity.BizCategory;
import com.halohub.frankenstein.mapper.BizCategoryMapper;
import com.halohub.frankenstein.vo.CategoryTreeVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminCategoryService {

    private final BizCategoryMapper bizCategoryMapper;

    public AdminCategoryService(BizCategoryMapper bizCategoryMapper) {
        this.bizCategoryMapper = bizCategoryMapper;
    }

    public List<CategoryTreeVO> listTree() {
        return buildTree(listAllCategories(), false);
    }

    public List<CategoryTreeVO> listActiveTree() {
        return buildTree(listAllCategories().stream()
                .filter(category -> category.getStatus() != null
                        && category.getStatus() == AuthConstants.STATUS_ACTIVE)
                .toList(), true);
    }

    @Transactional(rollbackFor = Exception.class)
    public CategoryTreeVO createCategory(CategoryCreateRequest request) {
        long parentId = request.getParentId() == null
                ? CategoryConstants.ROOT_PARENT_ID
                : request.getParentId();
        BizCategory parent = null;
        if (parentId != CategoryConstants.ROOT_PARENT_ID) {
            parent = getCategoryOrThrow(parentId);
            if (parent.getStatus() == null || parent.getStatus() != AuthConstants.STATUS_ACTIVE) {
                throw new BusinessException(CommonErrorCode.STATUS_ERROR);
            }
        }

        BizCategory category = new BizCategory();
        category.setParentId(parentId);
        category.setName(request.getName().trim());
        category.setIcon(request.getIcon());
        category.setSort(request.getSort() == null ? 0 : request.getSort());
        category.setLevel(parent == null ? 1 : parent.getLevel() + 1);
        category.setStatus(request.getStatus());
        bizCategoryMapper.insert(category);
        return toTreeVO(category);
    }

    @Transactional(rollbackFor = Exception.class)
    public CategoryTreeVO updateCategory(Long categoryId, CategoryUpdateRequest request) {
        BizCategory category = getCategoryOrThrow(categoryId);
        category.setName(request.getName().trim());
        category.setIcon(request.getIcon());
        if (request.getSort() != null) {
            category.setSort(request.getSort());
        }
        category.setStatus(request.getStatus());
        bizCategoryMapper.updateById(category);
        return toTreeVO(category);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long categoryId) {
        getCategoryOrThrow(categoryId);
        if (bizCategoryMapper.countChildren(categoryId) > 0) {
            throw new BusinessException(CommonErrorCode.DELETION_NOT_ALLOWED);
        }
        if (bizCategoryMapper.countSpuByCategory(categoryId) > 0) {
            throw new BusinessException(CommonErrorCode.DELETION_NOT_ALLOWED);
        }
        bizCategoryMapper.deleteById(categoryId);
    }

    public BizCategory getCategoryOrThrow(Long categoryId) {
        BizCategory category = bizCategoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException(CommonErrorCode.NO_CORRESPONDING_DATA);
        }
        return category;
    }

    private List<BizCategory> listAllCategories() {
        return bizCategoryMapper.selectList(new LambdaQueryWrapper<BizCategory>()
                .orderByAsc(BizCategory::getSort)
                .orderByAsc(BizCategory::getId));
    }

    private List<CategoryTreeVO> buildTree(List<BizCategory> categories, boolean pruneInactiveChildren) {
        if (categories.isEmpty()) {
            return List.of();
        }
        Map<Long, CategoryTreeVO> nodeMap = categories.stream()
                .collect(Collectors.toMap(BizCategory::getId, this::toTreeVO, (a, b) -> a));
        List<CategoryTreeVO> roots = new ArrayList<>();
        for (BizCategory category : categories) {
            CategoryTreeVO node = nodeMap.get(category.getId());
            Long parentId = category.getParentId() == null
                    ? CategoryConstants.ROOT_PARENT_ID
                    : category.getParentId();
            if (parentId == CategoryConstants.ROOT_PARENT_ID || !nodeMap.containsKey(parentId)) {
                roots.add(node);
            } else {
                nodeMap.get(parentId).getChildren().add(node);
            }
        }
        sortTree(roots);
        if (pruneInactiveChildren) {
            roots = roots.stream()
                    .map(this::pruneInactiveNode)
                    .filter(node -> node != null)
                    .toList();
        }
        return roots;
    }

    private CategoryTreeVO pruneInactiveNode(CategoryTreeVO node) {
        if (node.getStatus() == null || node.getStatus() != AuthConstants.STATUS_ACTIVE) {
            return null;
        }
        List<CategoryTreeVO> activeChildren = node.getChildren().stream()
                .map(this::pruneInactiveNode)
                .filter(child -> child != null)
                .toList();
        node.setChildren(new ArrayList<>(activeChildren));
        return node;
    }

    private void sortTree(List<CategoryTreeVO> nodes) {
        nodes.sort(Comparator.comparing(CategoryTreeVO::getSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(CategoryTreeVO::getId));
        for (CategoryTreeVO node : nodes) {
            sortTree(node.getChildren());
        }
    }

    private CategoryTreeVO toTreeVO(BizCategory category) {
        CategoryTreeVO vo = new CategoryTreeVO();
        BeanUtils.copyProperties(category, vo);
        return vo;
    }
}
