package com.home.vlad.servermanager.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.home.vlad.servermanager.dto.assistant.LLMPromptRequest;
import com.home.vlad.servermanager.service.assistant.LLMService;

@RestController
@RequestMapping("/manage/api/v1/llm")
public class LLMController {
    private LLMService assistantService;

    public LLMController(LLMService service) {
        this.assistantService = service;
    }

    @PostMapping("/preload")
    public void preloadModel() {
        assistantService.preloadModel();
    }

    @PostMapping("/prompt")
    public Map<String, String> handlePrompt(@RequestBody LLMPromptRequest request) {
        return assistantService.processPrompt(request);
    }
}
