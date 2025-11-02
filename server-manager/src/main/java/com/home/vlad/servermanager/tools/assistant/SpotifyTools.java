package com.home.vlad.servermanager.tools.assistant;

import com.fasterxml.jackson.databind.JsonNode;
import com.home.vlad.servermanager.config.SpotifyConfig;
import com.home.vlad.servermanager.service.assistant.HomeAssistantClient;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class SpotifyTools extends HomeAssistantTools {

    private SpotifyConfig spotifyConfig;

    public SpotifyTools(HomeAssistantClient haClient, SpotifyConfig spotifyConfig) {
        super(haClient);
        this.spotifyConfig = spotifyConfig;
    }

    @Tool(name = "spotify_set_playback", description = "Start or stop Spotify playback. If pausing, don't change volume."
            +
            "Args: play (true to start/resume, false to pause). ")
    public String setPlayback(boolean play) {

        log.info("Setting Spotify playback to {}", play ? "play" : "pause");

        Map<String, Object> payload = newPayload();
        payload.put("entity_id", this.spotifyConfig.getEntity_id());

        if (play) {
            // media_player.media_play
            return callServiceSimpleResponse(
                    "media_player",
                    "media_play",
                    payload,
                    "Resuming Spotify playback.");
        } else {
            // media_player.media_pause
            return callServiceSimpleResponse(
                    "media_player",
                    "media_pause",
                    payload,
                    "Pausing Spotify.");
        }
    }

    @Tool(name = "spotify_set_volume", description = "Set Spotify volume as a percentage (0-100). This tool DOES NOT start the music, call it in combination with set_playback if needed."
            +
            "Call this tool ONLY IF USER REQUESTS A VOLUME CHANGE. ")
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
                "Setting Spotify volume to " + pct + "%.");
    }

    @Tool(name = "spotify_list_playlists", description = "List Spotify playlists. DO NOT include IDs in response, just the titles.")
    public String listPlaylists() {
        String entityId = this.spotifyConfig.getEntity_id();

        JsonNode playlistsNode = haClient.browseMedia(entityId, "spotify://current_user_playlists",
                "spotify://current_user_playlists");

        List<Map<String, String>> items = new ArrayList<>();

        try {
            JsonNode playlistsNameNode = playlistsNode.get("children");
            for (JsonNode item : playlistsNameNode) {
                String title = textFromNode(item, "title");
                String id = textFromNode(item, "media_content_id");
                items.add(Map.of("id", id, "title", title));
            }
        } catch (Exception e) {
            log.error("Failed to parse Spotify playlists response: {}", playlistsNode.toString(), e);
            return "Error fetching Spotify playlists.";
        }

        if (items.isEmpty())
            return "[]";

        log.info("Fetched Spotify playlists: {}", items.toString());
        return items.toString();
    }

    @Tool(name = "spotify_play_playlist", description = "Start playback of a Spotify playlist by its media_content_id. You MUST call list_playlists first to get valid IDs.")
    public String playPlaylist(String mediaContentId) {
        if (mediaContentId == null || mediaContentId.isBlank()) {
            return "No mediaContentId provided.";
        }

        String entityId = spotifyConfig.getEntity_id();
        Map<String, Object> payload = newPayload();
        payload.put("entity_id", entityId);
        payload.put("media_content_id", mediaContentId);
        payload.put("media_content_type", "playlist");

        return callServiceSimpleResponse(
                "media_player",
                "play_media",
                payload,
                "Starting playlist.");
    }

    // ----------------- helpers -----------------

    private static String textFromNode(JsonNode n, String field) {
        return (n != null && n.hasNonNull(field)) ? n.get(field).asText("") : "";
    }
}
