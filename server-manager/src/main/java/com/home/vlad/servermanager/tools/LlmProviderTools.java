package com.home.vlad.servermanager.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import com.home.vlad.servermanager.service.assistant.LLMProviderManager;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LlmProviderTools {

    private final LLMProviderManager providerManager;

    @Tool(name = "set_llm_provider", description = "Switch the LLM provider for future requests. Either 'ollama' for local model, or 'openai' for cloud model. This tool is NOT schedulable.")
    public String setProvider(String provider) {
        if (provider == null || provider.isBlank()) {
            return "No provider specified.";
        }
        String p = provider.toLowerCase();
        if (!p.equals("ollama") && !p.equals("openai")) {
            return "Unsupported provider: " + p;
        }
        providerManager.setGlobalProvider(p);
        return "Provider set to " + p;
    }
}
