package com.halohub.frankenstein.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminDetailVO extends AdminVO {

    private List<Long> roleIds;
}
