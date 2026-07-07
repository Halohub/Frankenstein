package com.halohub.frankenstein.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class RoleUpdateRequest {

    @NotBlank
    @Size(max = 64)
    private String roleName;

    @Size(max = 255)
    private String remark;

    @NotNull
    private Integer status;

    @NotEmpty
    private List<Long> permissionIds;
}
