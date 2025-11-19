package com.home.vlad.servermanager.controller;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.home.vlad.servermanager.dto.assistant.LLMPromptRequest;
import com.home.vlad.servermanager.service.assistant.LLMProviderManager;
import com.home.vlad.servermanager.service.assistant.LLMService;
import com.home.vlad.servermanager.service.assistant.TTSService;

import jakarta.ws.rs.QueryParam;

@RestController
@RequestMapping("/manage/api/v1/llm")
public class LLMController {
    private LLMService assistantService;

    private LLMProviderManager providerManager;

    private final TTSService ttsService;

    public LLMController(LLMService service, LLMProviderManager providerManager, TTSService ttsService) {
        this.assistantService = service;
        this.providerManager = providerManager;
        this.ttsService = ttsService;
    }

    @PostMapping("/preload")
    public void preloadModel() {
        assistantService.preloadModel();
    }

    public void handlePromptHelper(LLMPromptRequest request,
            String LLMProviderHeader,
            Integer previousChatsCount) {
        try {
            if (LLMProviderHeader != null && !LLMProviderHeader.isBlank()) {
                providerManager.setRequestOverride(LLMProviderHeader);
            }
            if (previousChatsCount != null) {
                providerManager.setPreviousChatsCount(previousChatsCount);
            }
            assistantService.processPrompt(request);
        } finally {
            providerManager.clearRequestOverride();
            providerManager.clearUseMemoryOverride();
        }
    }

    @PostMapping("/prompt")
    public Map<String, String> handlePrompt(@RequestBody LLMPromptRequest request,
            @QueryParam("async") Boolean async,
            @RequestHeader(value = "X-LLM", required = false) String LLMProviderHeader,
            @RequestHeader(value = "X-Memory", required = false) Integer previousChatsCount) {

        if (async != null && async) {
            CompletableFuture.runAsync(() -> {
                handlePromptHelper(request, LLMProviderHeader, previousChatsCount);
            });
            return Map.of("status", "Processing started");
        } else {
            return assistantService.processPrompt(request);
        }
    }

    @PostMapping("/prompt/tts")
    public ResponseEntity<byte[]> handlePromptWithTTS(@RequestBody LLMPromptRequest request,
            @RequestHeader(value = "X-LLM", required = false) String LLMProviderHeader,
            @RequestHeader(value = "X-Memory", required = false) Integer previousChatsCount) throws Exception {

        String promptAnswer = assistantService.processPrompt(request).get("output");
        byte[] audioBytes = ttsService.textToSpeech(promptAnswer);

        return ResponseEntity
                .ok()
                .header("Content-Type", "audio/wav")
                .header("Content-Disposition", "inline; filename=\"tts.wav\"")
                .body(audioBytes);
    }
}
