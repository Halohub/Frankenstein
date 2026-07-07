package com.halohub.frankenstein.vo;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {

    private List<T> list;
    private Long total;

    public static <T> PageResult<T> of(List<T> list, Long total) {
        PageResult<T> pageResult = new PageResult<>();
        pageResult.setList(list);
        pageResult.setTotal(total);
        return pageResult;
    }
}
