package com.home.vlad.servermanager.service.vm;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.home.vlad.servermanager.config.GuacConfig;

@Service
public class GuacamoleService {
    private final GuacConfig guacConfig;

    private WebClient webClient = null;

    public GuacamoleService(GuacConfig guacConfig) {
        this.guacConfig = guacConfig;
        this.webClient = WebClient.builder()
                .baseUrl(guacConfig.getUrl())
                .build();
    }

    public String getGuacToken() {
        return webClient.post()
                .uri("/api/tokens")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters
                        .fromFormData("username", guacConfig.getUsername())
                        .with("password", guacConfig.getPassword()))
                .retrieve()
                .bodyToMono(GuacTokenResponse.class)
                .block().authToken;
    }

    public record GuacTokenResponse(String authToken, String username, String dataSource) {
    }
}
