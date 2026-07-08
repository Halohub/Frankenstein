package com.halohub.frankenstein.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class MemberPromoteRequest {

    @NotEmpty
    private List<Long> roleIds;

    @Size(min = 6, max = 32)
    private String password;
}
