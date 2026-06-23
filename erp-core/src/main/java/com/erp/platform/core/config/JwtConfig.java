package com.erp.platform.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    private String secret;
    private long accessExpirySeconds = 900;       // 15 minutes
    private long refreshExpirySeconds = 604800;   // 7 days
}
