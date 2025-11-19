package com.home.vlad.servermanager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "guacamole")
@Data
public class GuacConfig {
    private String url;
    private String username;
    private String password;
}
