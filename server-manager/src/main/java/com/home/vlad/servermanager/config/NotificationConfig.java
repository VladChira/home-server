package com.home.vlad.servermanager.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "gotify")
public class NotificationConfig {
    
    private String url;

    private String token;
}
