package com.home.vlad.servermanager.tools.assistant;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.home.vlad.servermanager.service.assistant.HomeAssistantClient;

@Component
public class HomeAssistantStatusTools extends HomeAssistantTools {
    // entity IDs we care about
    private static final String PING_SENSOR = "binary_sensor.192_168_0_67";
    private static final String TV_ENTITY_ID = "remote.samsung_q67aa_43_tv";

    public HomeAssistantStatusTools(HomeAssistantClient haClient) {
        super(haClient);
    }

    @Tool(name = "is_vlad_home", description = "Check if Vlad is currently home. " +
            "Uses Wi-Fi presence of his phone and returns a short answer.")
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
}
