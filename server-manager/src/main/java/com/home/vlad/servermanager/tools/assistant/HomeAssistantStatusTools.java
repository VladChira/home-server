package com.home.vlad.servermanager.tools.assistant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.home.vlad.servermanager.service.assistant.HomeAssistantClient;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class HomeAssistantStatusTools extends BaseHomeAssistantTools {
    // entity IDs we care about
    private static final String PING_SENSOR = "binary_sensor.192_168_0_67";
    private static final String TV_ENTITY_ID = "remote.samsung_q67aa_43_tv";

    public HomeAssistantStatusTools(HomeAssistantClient haClient) {
        super(haClient);
    }

    @Tool(name = "is_vlad_home", description = "Check if Vlad is currently home.")
    public String isVladHome() {
        JsonNode stateNode = haClient.getEntityState(PING_SENSOR);
        String state = stateNode.path("state").asText(""); // usually "on" or "off"
        boolean isHome = "on".equalsIgnoreCase(state);

        return isHome ? "Vlad is home." : "Vlad is away.";
    }

    @Tool(name = "get_tv_status", description = "Check if the living room TV is currently on or off.")
    public String getTvStatus() {
        JsonNode stateNode = haClient.getEntityState(TV_ENTITY_ID);
        String rawState = stateNode.path("state").asText("").trim();
        if (rawState.isEmpty()) {
            rawState = "unknown";
        }
        return "TV is currently " + rawState + ".";
    }

    @Tool(name = "lights_get_status", description = "Return all lights in the house. Brightness is 0-100. Color/effect may be empty.")
    public String getLightsStatus() {
        JsonNode states = haClient.getStates(); // GET /api/states
        List<String> items = new ArrayList<>();
        if (states != null && states.isArray()) {
            for (JsonNode s : states) {
                String entityId = s.path("entity_id").asText("");
                if (!entityId.startsWith("light."))
                    continue;

                JsonNode attr = s.path("attributes");
                String friendly = attr.path("friendly_name").asText(entityId);
                boolean on = "on".equalsIgnoreCase(s.path("state").asText(""));
                // HA brightness is 0-255; convert to ~0-100
                int bri = -1;
                if (attr.has("brightness")) {
                    int haBri = attr.get("brightness").asInt();
                    bri = Math.round(haBri * 100f / 255f);
                }
                String effect = attr.path("effect").asText("");
                String colorName = ""; // we keep it simple for now

                StringBuilder sb = new StringBuilder();
                sb.append("{\"id\":\"").append(esc(entityId)).append("\"")
                        .append(",\"name\":\"").append(esc(friendly)).append("\"")
                        .append(",\"on\":").append(on);
                if (bri >= 0) {
                    sb.append(",\"brightness\":").append(bri);
                }
                if (!colorName.isEmpty()) {
                    sb.append(",\"color\":\"").append(esc(colorName)).append("\"");
                }
                if (!effect.isEmpty()) {
                    sb.append(",\"effect\":\"").append(esc(effect)).append("\"");
                }
                sb.append("}");
                items.add(sb.toString());
            }
        }
        String finalAnswer = "[" + String.join(",", items) + "]";
        log.info("Returning lights status: {}", finalAnswer);
        return finalAnswer;
    }

    @Tool(name = "lights_set_params", description = "YOU MUST CALL lights_get_status BEFORE THIS to see entity IDs. Set params on a specific light. Args: entity_id. "
            + "Optional: brightness (0-100), effect (string, oneOf: Cozy, Romance, White, Jungle)")
    public String setLightParams(String entityId, Integer brightness, String effect) {

        log.info("Setting light params for {}: brightness={}, effect={}", entityId, brightness, effect);

        if (entityId == null || entityId.isBlank()) {
            return "Light entity_id is required.";
        }

        Map<String, Object> payload = newPayload();
        payload.put("entity_id", entityId);

        if (brightness != null) {
            // convert 0-100 -> 0-255
            int bri = Math.max(0, Math.min(100, brightness));
            int haBri = Math.round(bri * 255f / 100f);
            payload.put("brightness", haBri);
        }
        if (effect != null && !effect.isBlank()) {
            payload.put("effect", effect);
        }

        return callServiceSimpleResponse(
                "light",
                "turn_on",
                payload,
                "Updated light " + entityId);
    }

    private String esc(String s) {
        if (s == null)
            return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
