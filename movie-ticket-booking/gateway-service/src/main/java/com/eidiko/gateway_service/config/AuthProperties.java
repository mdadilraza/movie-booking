package com.eidiko.gateway_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "gateway.auth")
@Getter
@Setter
public class AuthProperties {
    private List<String> openEndpoints;
}
