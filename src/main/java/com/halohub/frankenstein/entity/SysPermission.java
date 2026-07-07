package com.halohub.frankenstein.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_permission")
public class SysPermission {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String permCode;
    private String permName;
    private String permType;
    private Long parentId;
    private String path;
    private String component;
    private String icon;
    private Integer hidden;
    private Integer sort;
    private Integer status;
    private String apiPath;
    private String method;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
