package com.halohub.frankenstein.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MemberRegisterRequest {

    @NotBlank
    @Size(min = 4, max = 32)
    private String username;

    @NotBlank
    @Size(min = 6, max = 32)
    private String password;

    private String nickname;
    private String phone;
    private String email;
}
