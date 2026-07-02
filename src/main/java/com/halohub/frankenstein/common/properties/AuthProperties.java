package com.halohub.frankenstein.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "frankenstein.auth")
public class AuthProperties {

    private int loginMaxFailures = 5;
    private int loginLockMinutes = 15;
    private int memberMaxDevicesNormal = 3;
    private int memberMaxDevicesVip = 10;
}
