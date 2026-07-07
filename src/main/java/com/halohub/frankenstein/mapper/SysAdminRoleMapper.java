package com.halohub.frankenstein.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysAdminRoleMapper {

    @Insert("INSERT INTO sys_admin_role (admin_id, role_id) " +
            "SELECT #{adminId}, id FROM sys_role WHERE role_code = #{roleCode} LIMIT 1")
    int bindRole(@Param("adminId") Long adminId, @Param("roleCode") String roleCode);
}
