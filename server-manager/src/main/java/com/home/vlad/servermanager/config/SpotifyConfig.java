package com.home.vlad.servermanager.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spotify")
public class SpotifyConfig {
    private String entity_id;
}