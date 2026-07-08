package com.halohub.frankenstein.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysAdminRoleMapper {

    @Select("SELECT role_id FROM sys_admin_role WHERE admin_id = #{adminId}")
    List<Long> listRoleIdsByAdminId(@Param("adminId") Long adminId);

    @Delete("DELETE FROM sys_admin_role WHERE admin_id = #{adminId}")
    int deleteByAdminId(@Param("adminId") Long adminId);

    @Insert("<script>" +
            "INSERT INTO sys_admin_role (admin_id, role_id) VALUES " +
            "<foreach collection='roleIds' item='roleId' separator=','>" +
            "(#{adminId}, #{roleId})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("adminId") Long adminId, @Param("roleIds") List<Long> roleIds);

    @Select("SELECT COUNT(1) FROM sys_admin_role ar " +
            "INNER JOIN sys_role r ON ar.role_id = r.id " +
            "WHERE ar.admin_id = #{adminId} AND r.role_code = #{roleCode} AND r.deleted = 0")
    long countRoleByAdminIdAndCode(@Param("adminId") Long adminId, @Param("roleCode") String roleCode);

    @Select("SELECT COUNT(DISTINCT ar.admin_id) FROM sys_admin_role ar " +
            "INNER JOIN sys_role r ON ar.role_id = r.id " +
            "WHERE r.role_code = #{roleCode} AND r.deleted = 0")
    long countAdminsByRoleCode(@Param("roleCode") String roleCode);
}
