package com.home.vlad.servermanager.config;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.home.vlad.servermanager.tools.TaskSchedulingTools;
import com.home.vlad.servermanager.tools.homeassistant.HomeAssistantScriptTools;
import com.home.vlad.servermanager.tools.homeassistant.HomeAssistantStatusTools;
import com.home.vlad.servermanager.tools.homeassistant.SpotifyTools;

@Configuration
public class McpConfig {

    @Bean
    public ToolCallbackProvider toolCallbackProvider(HomeAssistantScriptTools scriptTools,
            HomeAssistantStatusTools statusTools, TaskSchedulingTools taskSchedulingTools, SpotifyTools spotifyPlaybackTools) {
        return MethodToolCallbackProvider
                .builder()
                .toolObjects(
                        scriptTools,
                        statusTools,
                        taskSchedulingTools,
                        spotifyPlaybackTools
                )
                .build();
    }
}
