package com.home.vlad.servermanager.tools.homeassistant;

import com.home.vlad.servermanager.config.SpotifyConfig;
import com.home.vlad.servermanager.service.assistant.HomeAssistantClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SpotifyTools extends HomeAssistantTools {

    private SpotifyConfig spotifyConfig;

    public SpotifyTools(HomeAssistantClient haClient, SpotifyConfig spotifyConfig) {
        super(haClient);
        this.spotifyConfig = spotifyConfig;
    }

    @Tool(
        name = "spotify_set_playback",
        description = "Start or stop Spotify playback. " +
                      "Args: play (true to start/resume, false to pause)."
    )
    public String setPlayback(boolean play) {
        Map<String, Object> payload = newPayload();
        payload.put("entity_id", this.spotifyConfig.getEntity_id());

        if (play) {
            // media_player.media_play
            return callServiceSimpleResponse(
                "media_player",
                "media_play",
                payload,
                "Resuming Spotify playback."
            );
        } else {
            // media_player.media_pause
            return callServiceSimpleResponse(
                "media_player",
                "media_pause",
                payload,
                "Pausing Spotify."
            );
        }
    }

    @Tool(
        name = "spotify_set_volume",
        description = "Set Spotify volume as a percentage (0-100). " +
                      "Args: percent (int 0-100).."
    )
    public String setVolume(int volume) {
        int pct = Math.max(0, Math.min(100, volume));

        double level = pct / 100.0; // HA expects 0.0–1.0

        Map<String, Object> payload = newPayload();
        payload.put("entity_id", this.spotifyConfig.getEntity_id());
        payload.put("volume_level", level);

        return callServiceSimpleResponse(
            "media_player",
            "volume_set",
            payload,
            "Setting Spotify volume to " + pct + "%."
        );
    }
}
