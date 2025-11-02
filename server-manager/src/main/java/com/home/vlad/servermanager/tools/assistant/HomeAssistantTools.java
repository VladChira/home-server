package com.home.vlad.servermanager.tools.assistant;

import java.util.HashMap;
import java.util.Map;

import com.home.vlad.servermanager.service.assistant.HomeAssistantClient;

/**
 * Base class for all Home Assistant-related tool collections.
 * 
 * Subclasses (e.g. script tools, light tools, sensor tools) get:
 * - Access to the HomeAssistantClient
 * - Helpers to build payloads and invoke HA services
 * - A consistent summary format to return back to the LLM
 */
public abstract class HomeAssistantTools {

    protected final HomeAssistantClient haClient;

    protected HomeAssistantTools(HomeAssistantClient haClient) {
        this.haClient = haClient;
    }

    /**
     * Convenience to start a mutable payload map.
     */
    protected Map<String, Object> newPayload() {
        return new HashMap<>();
    }

    /**
     * Call a Home Assistant service and return a response string that is
     * friendly for the model to read back to the user.
     *
     * @param domain  e.g. "script", "light", "climate"
     * @param service e.g. "good_night", "turn_off", "set_temperature"
     * @param payload map of args to HA
     */
    protected String callServiceSimpleResponse(String domain,
            String service,
            Map<String, Object> payload, String actionDescription) {

        haClient.callService(domain, service, payload);

        // Return a compact "it worked" message for the model to speak back.
        return actionDescription != null && !actionDescription.isBlank()
                ? actionDescription
                : ("Action '" + domain + "." + service + "' executed successfully.");
    }

    protected String callServiceAndReturnDebug(String domain,
            String service,
            Map<String, Object> payload) {

        String haRaw = haClient.callService(domain, service, payload);

        return """
                Called Home Assistant service %s.%s
                Payload sent: %s
                Raw HA response: %s
                """.formatted(domain, service, payload, haRaw);
    }
}