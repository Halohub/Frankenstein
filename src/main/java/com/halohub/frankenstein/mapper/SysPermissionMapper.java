package com.halohub.frankenstein.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.halohub.frankenstein.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    @Select("SELECT * FROM sys_permission WHERE deleted = 0 AND status = 1 " +
            "AND perm_code LIKE CONCAT(#{prefix}, '%') ORDER BY sort ASC, id ASC")
    List<SysPermission> listActiveByCodePrefix(@Param("prefix") String prefix);

    @Select("SELECT DISTINCT p.* FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN sys_admin_role ar ON rp.role_id = ar.role_id " +
            "WHERE ar.admin_id = #{adminId} AND p.deleted = 0 AND p.status = 1 " +
            "ORDER BY p.sort ASC, p.id ASC")
    List<SysPermission> listActiveByAdminId(@Param("adminId") Long adminId);
}
