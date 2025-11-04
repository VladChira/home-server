package com.home.vlad.servermanager.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.home.vlad.servermanager.dto.assistant.LLMPromptRequest;
import com.home.vlad.servermanager.service.assistant.LLMProviderManager;
import com.home.vlad.servermanager.service.assistant.LLMService;

@RestController
@RequestMapping("/manage/api/v1/llm")
public class LLMController {
    private LLMService assistantService;

    private LLMProviderManager providerManager;

    public LLMController(LLMService service, LLMProviderManager providerManager) {
        this.assistantService = service;
        this.providerManager = providerManager;
    }

    @PostMapping("/preload")
    public void preloadModel() {
        assistantService.preloadModel();
    }

    @PostMapping("/prompt")
    public Map<String, String> handlePrompt(@RequestBody LLMPromptRequest request, @RequestHeader(value = "X-LLM", required = false) String headerProvider) {
        try {
            if (headerProvider != null && !headerProvider.isBlank()) {
                providerManager.setRequestOverride(headerProvider);  // highest priority
            }
            return assistantService.processPrompt(request);
        } finally {
            providerManager.clearRequestOverride();
        }
    }
}
