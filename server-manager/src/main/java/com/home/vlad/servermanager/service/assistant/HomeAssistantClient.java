package com.home.vlad.servermanager.service.assistant;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.home.vlad.servermanager.config.HomeAssistantConfig;
import com.home.vlad.servermanager.service.NotificationService;

import reactor.core.publisher.Mono;

/**
 * Single point of contact for Home Assistant.
 *
 * Responsibilities:
 * - POST service calls (script.turn_on, remote.turn_off, etc.)
 * - GET entity state (/api/states/{entity_id})
 * - GET all states (/api/states)
 *
 * Tools should ONLY talk to this client, never hit HA directly.
 */
@Service
public class HomeAssistantClient {

    private final Logger logger = LoggerFactory.getLogger(HomeAssistantClient.class);

    private final NotificationService notificationService;

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public HomeAssistantClient(HomeAssistantConfig config, NotificationService notificationService) {
        this.webClient = WebClient.builder()
                .baseUrl(config.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + config.getToken())
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.notificationService = notificationService;
    }

    /**
     * Call an arbitrary Home Assistant service endpoint.
     *
     * domain -> "script", "light", "remote", "climate", etc.
     * service -> "good_night", "turn_off", "turn_on", "set_temperature", etc.
     *
     * Example:
     * callService(
     * "script",
     * "good_night",
     * Map.of(
     * "entity_id", "script.good_night",
     * "announce", true
     * )
     * )
     *
     * Sends POST /api/services/script/good_night with that JSON body.
     *
     * Returns the raw JSON-ish response from HA as a String.
     * Rationale: some higher-level tools might want to inspect it for debugging.
     * Most tools will ignore the body and just assume success if we didn't throw.
     */
    public String callService(String domain, String service, Map<String, Object> payload) {

        notificationService.sendSilent("HA Service Call", String.format("%s/%s: %s", domain, service, payload));

        if (payload == null) {
            payload = Map.of();
        }

        Mono<String> responseMono = webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/services/{domain}/{service}")
                        .build(domain, service))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class);

        String response = responseMono.block();

        logger.info(
                "Called Home Assistant service {}/{} with payload {}. Response: {}",
                domain, service, payload, response);

        return response;
    }

    /**
     * Fetch a single entity's full state object from Home Assistant.
     *
     * GET /api/states/{entity_id}
     *
     * Returns parsed JSON:
     * {
     * "entity_id": "...",
     * "state": "on"/"off"/"...",
     * "attributes": { ... },
     * ...
     * }
     *
     * Throws RuntimeException if HA response can't be parsed.
     */
    public JsonNode getEntityState(String entityId) {

        notificationService.sendSilent("HA State Fetch", "Fetching state for " + entityId);

        Mono<String> responseMono = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/states/{entityId}")
                        .build(entityId))
                .retrieve()
                .bodyToMono(String.class);

        String body = responseMono.block();

        try {
            JsonNode node = objectMapper.readTree(body);
            logger.debug("Fetched state for {}: {}", entityId, body);
            return node;
        } catch (Exception e) {
            logger.error("Failed to parse HA state for {}. Body: {}", entityId, body, e);
            throw new RuntimeException(
                    "Failed to parse HA state for " + entityId + ": " + body, e);
        }
    }

    /**
     * Fetch ALL entity states.
     *
     * GET /api/states
     *
     * Returns a JsonNode array, each element same shape as getEntityState().
     *
     * Callers should filter what they need (e.g. only light.*).
     */
    public JsonNode getAllStates() {

        notificationService.sendSilent("HA State Fetch", "Fetching all states");

        Mono<String> responseMono = webClient.get()
                .uri("/api/states")
                .retrieve()
                .bodyToMono(String.class);

        String body = responseMono.block();

        try {
            JsonNode node = objectMapper.readTree(body);
            logger.debug("Fetched all HA states ({} bytes)", body.length());
            return node;
        } catch (Exception e) {
            logger.error("Failed to parse HA states list", e);
            throw new RuntimeException("Failed to parse HA states list: " + body, e);
        }
    }

    public JsonNode browseMedia(String entityId, String mediaContentId, String mediaContentType) {
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("entity_id", entityId);
        if (mediaContentId != null && !mediaContentId.isBlank()) {
            body.put("media_content_id", mediaContentId);
        }
        if (mediaContentType != null && !mediaContentType.isBlank()) {
            body.put("media_content_type", mediaContentType);
        }

        // NOTE: ?return_response is required for HA to include the payload
        String raw = webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/services/media_player/browse_media")
                        .queryParam("return_response") // key-only param
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            JsonNode root = objectMapper.readTree(raw);
            // unwrap service_response.{entityId}
            JsonNode sr = root.path("service_response");
            if (sr.isObject()) {
                // prefer exact entity key; if absent, take the first object value
                JsonNode byKey = sr.path(entityId);
                if (!byKey.isMissingNode() && !byKey.isNull()) {
                    return byKey;
                }
                // fallback: first child
                var it = sr.fields();
                if (it.hasNext()) {
                    return it.next().getValue();
                }
            }
            throw new RuntimeException("Unexpected browse_media response shape: " + raw);
        } catch (Exception e) {
            logger.warn("browse_media parse error: {}", e.getMessage());
            throw new RuntimeException("Failed to parse browse_media response", e);
        }
    }

    public JsonNode getShoppingList() {
        String raw = webClient.get()
                .uri("/api/shopping_list")
                .retrieve()
                .bodyToMono(String.class)
                .block();
        try {
            return objectMapper.readTree(raw);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse shopping list", e);
        }
    }
}
