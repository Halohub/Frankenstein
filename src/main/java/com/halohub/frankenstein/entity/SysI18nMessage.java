package com.halohub.frankenstein.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_i18n_message")
public class SysI18nMessage {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String refType;
    private Long refId;
    private String locale;
    private String fieldName;
    private String fieldValue;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
