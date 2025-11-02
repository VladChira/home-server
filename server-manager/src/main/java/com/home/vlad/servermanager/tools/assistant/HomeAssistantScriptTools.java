package com.home.vlad.servermanager.tools.assistant;

import java.util.Map;

import org.slf4j.Logger;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import com.home.vlad.servermanager.service.assistant.HomeAssistantClient;

/**
 * Tools that wrap Home Assistant scripts (domain "script").
 */
@Component
public class HomeAssistantScriptTools extends HomeAssistantTools {

    Logger logger = org.slf4j.LoggerFactory.getLogger(HomeAssistantScriptTools.class);

    public HomeAssistantScriptTools(HomeAssistantClient haClient) {
        super(haClient);
    }

    private String callScriptToolSimpleResponse(String scriptName,
            String actionDescription, Map<String, Object> payload) {
        String response = callServiceSimpleResponse(
                "script",
                scriptName,
                payload,
                actionDescription);
        logger.info("Home Assistant response: {}", response);
        return response;
    }

    // -------------------- SCRIPT DEFINITIONS BELOW --------------------

    @Tool(name = "global_cozy_script", description = "Sets the mood to cozy in the entire house.")
    public String runGlobalCozy() {
        return callScriptToolSimpleResponse("global_cozy", "Global cozy script called successfully", newPayload());
    }

    @Tool(name = "global_warm_white", description = "Sets all the lights to warm white.")
    public String runGlobalWarmWhite() {
        return callScriptToolSimpleResponse("global_warm_white", "Global warm white script called successfully",
                newPayload());
    }

    @Tool(name = "all_lights_off", description = "Turns off all the lights in the house.")
    public String runTurnOffAllLights() {
        return callScriptToolSimpleResponse("lights_off", "Turn off all lights script called successfully",
                newPayload());
    }

    @Tool(name = "bedtime", description = "Sets the time to bedtime mode.")
    public String runBedtime() {
        return callScriptToolSimpleResponse("bedtime", "Bedtime script called successfully",
                newPayload());
    }
}
