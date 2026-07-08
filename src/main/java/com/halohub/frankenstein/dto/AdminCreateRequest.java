package com.halohub.frankenstein.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class AdminCreateRequest {

    @NotBlank
    @Size(min = 4, max = 32)
    private String username;

    @NotBlank
    @Size(min = 6, max = 32)
    private String password;

    private String nickname;
    private String phone;
    private String email;

    @NotNull
    private Integer status;

    @NotEmpty
    private List<Long> roleIds;
}
