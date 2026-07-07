package com.halohub.frankenstein.vo;

import lombok.Data;

import java.util.List;

@Data
public class RouteMenuVO {

    private String path;
    private String name;
    private String component;
    private String redirect;
    private RouteMenuMetaVO meta;
    private List<RouteMenuVO> children;
}
