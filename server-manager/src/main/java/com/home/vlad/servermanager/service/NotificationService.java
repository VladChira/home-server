package com.home.vlad.servermanager.service;

import com.home.vlad.servermanager.config.NotificationConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@Slf4j
public class NotificationService {

    private final WebClient client;

    public NotificationService(NotificationConfig cfg) {
        this.client = WebClient.builder()
                .baseUrl(cfg.getUrl())
                .defaultHeader("X-Gotify-Key", cfg.getToken()) // auth
                .build();
    }

    /**
     * Send a Gotify notification.
     * @param title    short title
     * @param message  body text
     * @param priority Gotify priority (1 = lowest/silent)
     */
    public void send(String title, String message, int priority) {
        try {
            client.post()
                  .uri("/message")
                  .contentType(MediaType.APPLICATION_JSON)
                  .bodyValue(Map.of(
                          "title", title,
                          "message", message,
                          "priority", priority
                  ))
                  .retrieve()
                  .bodyToMono(String.class)
                  .block();
        } catch (Exception e) {
            log.warn("Failed to send Gotify notification: {}", e.getMessage());
        }
    }

    /** Convenience for quiet pushes */
    public void sendSilent(String title, String message) {
        send(title, message, 1);
    }
}
