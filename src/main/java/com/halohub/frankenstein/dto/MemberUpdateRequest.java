package com.halohub.frankenstein.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MemberUpdateRequest {

    private String nickname;
    private String phone;
    private String email;

    @NotNull
    private Integer status;

    @NotNull
    private Integer vipLevel;
}
