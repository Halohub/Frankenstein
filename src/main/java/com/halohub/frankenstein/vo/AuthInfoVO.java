package com.halohub.frankenstein.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AuthInfoVO {

    private Long id;
    private String username;
    private String nickname;
    private Integer vipLevel;
    private List<String> roles;
    private List<String> permissions;
}
