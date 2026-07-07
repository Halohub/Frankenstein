package com.halohub.frankenstein.vo;

import lombok.Data;

import java.util.List;

@Data
public class PermissionTreeVO {

    private Long id;
    private String permCode;
    private String permName;
    private String permType;
    private Long parentId;
    private List<PermissionTreeVO> children;
}
