package com.halohub.frankenstein.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class RoleDetailVO extends RoleVO {

    private List<Long> permissionIds;
}
