package com.home.vlad.servermanager.config;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.home.vlad.servermanager.tools.LlmProviderTools;
import com.home.vlad.servermanager.tools.TaskSchedulingTools;
import com.home.vlad.servermanager.tools.assistant.ComposedTools;
import com.home.vlad.servermanager.tools.assistant.HomeAssistantScriptTools;
import com.home.vlad.servermanager.tools.assistant.HomeAssistantStatusTools;
import com.home.vlad.servermanager.tools.assistant.ShoppingListTools;
import com.home.vlad.servermanager.tools.assistant.SpotifyTools;

@Configuration
public class McpConfig {

    @Bean
    public ToolCallbackProvider toolCallbackProvider(HomeAssistantScriptTools scriptTools,
            HomeAssistantStatusTools statusTools, TaskSchedulingTools taskSchedulingTools,
            SpotifyTools spotifyPlaybackTools, ComposedTools composedTools, LlmProviderTools llmProviderTools, ShoppingListTools shoppingListTools) {
        return MethodToolCallbackProvider
                .builder()
                .toolObjects(
                        scriptTools,
                        statusTools,
                        taskSchedulingTools,
                        spotifyPlaybackTools,
                        composedTools,
                        llmProviderTools,
                        shoppingListTools
                        )
                .build();
    }
}
