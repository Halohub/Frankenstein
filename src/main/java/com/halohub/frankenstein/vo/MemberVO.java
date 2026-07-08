package com.halohub.frankenstein.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MemberVO {

    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String email;
    private Integer vipLevel;
    private Integer status;
    private String registerSource;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<String> roles;
}
