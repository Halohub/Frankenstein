package com.halohub.frankenstein.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class AdminUpdateRequest {

    private String nickname;
    private String phone;
    private String email;

    @NotNull
    private Integer status;

    @Size(min = 6, max = 32)
    private String password;

    @NotEmpty
    private List<Long> roleIds;
}
