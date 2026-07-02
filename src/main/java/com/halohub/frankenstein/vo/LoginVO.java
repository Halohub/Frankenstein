package com.halohub.frankenstein.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LoginVO {

    private Long id;
    private String username;
    private String token;
    private String tokenType;
}
