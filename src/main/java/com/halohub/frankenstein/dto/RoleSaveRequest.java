package com.halohub.frankenstein.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class RoleSaveRequest {

    @NotBlank
    @Size(max = 64)
    @Pattern(regexp = "^[A-Z][A-Z0-9_]*$", message = "roleCode must be uppercase letters, digits, and underscores")
    private String roleCode;

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
