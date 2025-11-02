package com.home.vlad.servermanager.tools.assistant;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ComposedTools {
    private final HomeAssistantScriptTools scriptTools;

    private final HomeAssistantStatusTools statusTools;

    private final SpotifyTools spotifyTools;

    @Tool(name = "guest_mode", description = "Sets the house to guest mode. This will also disable wake word detection for privacy. Inform the user of this.")
    public String setGuestMode() {
        scriptTools.runGlobalCozy();
        spotifyTools.setPlayback(true);

        return "Guest mode activated.";
    }

    @Tool(name = "leaving_home", description = "Prepares the house for when the user is leaving home.")
    public String leavingHome() {
        scriptTools.runTurnOffAllLights();
        spotifyTools.setPlayback(false);

        return "Leaving home mode activated.";
    }
}
