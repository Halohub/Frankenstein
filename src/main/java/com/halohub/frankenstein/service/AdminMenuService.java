package com.halohub.frankenstein.service;

import com.halohub.frankenstein.common.constant.PermissionConstants;
import com.halohub.frankenstein.common.util.PermCodeUtils;
import com.halohub.frankenstein.entity.SysPermission;
import com.halohub.frankenstein.mapper.SysAdminMapper;
import com.halohub.frankenstein.mapper.SysPermissionMapper;
import com.halohub.frankenstein.vo.RouteMenuMetaVO;
import com.halohub.frankenstein.vo.RouteMenuVO;
import com.halohub.frankenstein.vo.PermissionTreeVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminMenuService {

    private final SysPermissionMapper sysPermissionMapper;
    private final SysAdminMapper sysAdminMapper;
    private final I18nMessageService i18nMessageService;

    public AdminMenuService(SysPermissionMapper sysPermissionMapper,
                            SysAdminMapper sysAdminMapper,
                            I18nMessageService i18nMessageService) {
        this.sysPermissionMapper = sysPermissionMapper;
        this.sysAdminMapper = sysAdminMapper;
        this.i18nMessageService = i18nMessageService;
    }

    public List<RouteMenuVO> listMenus(Long adminId, Locale locale) {
        List<SysPermission> menuPermissions = resolveAccessibleMenus(adminId);
        Map<Long, String> translatedNames = i18nMessageService.batchResolvePermissionNames(
                menuPermissions.stream().map(SysPermission::getId).toList(),
                i18nMessageService.normalize(locale));
        List<SysPermission> roots = menuPermissions.stream()
                .filter(permission -> permission.getParentId() == null || permission.getParentId() == 0L)
                .sorted(menuComparator())
                .toList();
        return roots.stream()
                .map(permission -> toRouteMenu(permission, menuPermissions, translatedNames))
                .toList();
    }

    public List<PermissionTreeVO> listPermissionTree(Locale locale) {
        List<SysPermission> permissions = sysPermissionMapper.listActiveByCodePrefix(PermissionConstants.ADMIN_PERM_PREFIX);
        Map<Long, String> translatedNames = i18nMessageService.batchResolvePermissionNames(
                permissions.stream().map(SysPermission::getId).toList(),
                i18nMessageService.normalize(locale));
        List<SysPermission> roots = permissions.stream()
                .filter(permission -> permission.getParentId() == null || permission.getParentId() == 0L)
                .sorted(menuComparator())
                .toList();
        return roots.stream()
                .map(permission -> toPermissionTree(permission, permissions, translatedNames))
                .toList();
    }

    public boolean isSuperAdmin(Long adminId) {
        return sysAdminMapper.listRoleCodesByAdminId(adminId).contains(PermissionConstants.ROLE_ADMIN_SUPER);
    }

    public List<String> listAllAdminPermCodes() {
        return sysPermissionMapper.listActiveByCodePrefix(PermissionConstants.ADMIN_PERM_PREFIX).stream()
                .map(SysPermission::getPermCode)
                .sorted()
                .toList();
    }

    private List<SysPermission> resolveAccessibleMenus(Long adminId) {
        List<SysPermission> allMenus = sysPermissionMapper.listActiveByCodePrefix(PermissionConstants.ADMIN_PERM_PREFIX)
                .stream()
                .filter(permission -> PermissionConstants.TYPE_MENU.equals(permission.getPermType()))
                .toList();
        if (isSuperAdmin(adminId)) {
            return allMenus;
        }
        Set<Long> assignedIds = sysPermissionMapper.listActiveByAdminId(adminId).stream()
                .map(SysPermission::getId)
                .collect(Collectors.toSet());
        Map<Long, SysPermission> menuMap = allMenus.stream()
                .collect(Collectors.toMap(SysPermission::getId, permission -> permission));
        Set<Long> allowedMenuIds = new HashSet<>();
        for (SysPermission menu : allMenus) {
            if (assignedIds.contains(menu.getId())) {
                allowedMenuIds.add(menu.getId());
                addAncestors(menu, menuMap, allowedMenuIds);
            }
        }
        return allMenus.stream()
                .filter(menu -> allowedMenuIds.contains(menu.getId()))
                .toList();
    }

    private void addAncestors(SysPermission permission,
                              Map<Long, SysPermission> menuMap,
                              Set<Long> allowedMenuIds) {
        Long parentId = permission.getParentId();
        while (parentId != null && parentId > 0L) {
            if (!allowedMenuIds.add(parentId)) {
                break;
            }
            SysPermission parent = menuMap.get(parentId);
            if (parent == null) {
                break;
            }
            parentId = parent.getParentId();
        }
    }

    private RouteMenuVO toRouteMenu(SysPermission permission,
                                    List<SysPermission> allMenus,
                                    Map<Long, String> translatedNames) {
        RouteMenuVO route = new RouteMenuVO();
        route.setPath(permission.getPath());
        route.setName(PermCodeUtils.toRouteName(permission.getPermCode()));
        route.setComponent(permission.getComponent());

        RouteMenuMetaVO meta = new RouteMenuMetaVO();
        meta.setTitle(translatedNames.getOrDefault(permission.getId(), permission.getPermName()));
        meta.setIcon(permission.getIcon());
        if (permission.getHidden() != null && permission.getHidden() == 1) {
            meta.setHidden(true);
        }
        route.setMeta(meta);

        List<SysPermission> children = allMenus.stream()
                .filter(item -> Objects.equals(permission.getId(), item.getParentId()))
                .sorted(menuComparator())
                .toList();
        if (!children.isEmpty()) {
            route.setChildren(children.stream()
                    .map(child -> toRouteMenu(child, allMenus, translatedNames))
                    .toList());
            route.setRedirect(buildRedirect(route));
        }
        return route;
    }

    private String buildRedirect(RouteMenuVO parent) {
        if (parent.getChildren() == null || parent.getChildren().isEmpty()) {
            return null;
        }
        RouteMenuVO firstChild = parent.getChildren().get(0);
        String childPath = firstChild.getPath();
        if (childPath == null) {
            return null;
        }
        if (childPath.startsWith("/")) {
            return childPath;
        }
        String parentPath = parent.getPath() == null ? "" : parent.getPath();
        if (!parentPath.startsWith("/")) {
            parentPath = "/" + parentPath;
        }
        return parentPath.replaceAll("/+$", "") + "/" + childPath.replaceAll("^/+", "");
    }

    private PermissionTreeVO toPermissionTree(SysPermission permission,
                                              List<SysPermission> allPermissions,
                                              Map<Long, String> translatedNames) {
        PermissionTreeVO node = new PermissionTreeVO();
        node.setId(permission.getId());
        node.setPermCode(permission.getPermCode());
        node.setPermName(translatedNames.getOrDefault(permission.getId(), permission.getPermName()));
        node.setPermType(permission.getPermType());
        node.setParentId(permission.getParentId());

        List<SysPermission> children = allPermissions.stream()
                .filter(item -> Objects.equals(permission.getId(), item.getParentId()))
                .sorted(menuComparator())
                .toList();
        if (!children.isEmpty()) {
            node.setChildren(children.stream()
                    .map(child -> toPermissionTree(child, allPermissions, translatedNames))
                    .collect(Collectors.toCollection(ArrayList::new)));
        } else {
            node.setChildren(List.of());
        }
        return node;
    }

    private Comparator<SysPermission> menuComparator() {
        return Comparator.comparing(SysPermission::getSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(SysPermission::getId);
    }
}
