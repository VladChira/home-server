package com.home.vlad.servermanager.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.home.vlad.servermanager.dto.assistant.LLMPromptRequest;
import com.home.vlad.servermanager.service.assistant.LLMService;
import com.home.vlad.servermanager.service.assistant.STTService;

@RestController
@RequestMapping("/manage/api/v1/voice")
public class VoiceAssistantController {
    LLMService llmService;
    STTService sttService;

    Logger logger = LoggerFactory.getLogger(VoiceAssistantController.class);

    public VoiceAssistantController(LLMService llmService, STTService sttService) {
        this.llmService = llmService;
        this.sttService = sttService;
    }

    @PostMapping(path = "/command", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> runVoiceCommand(@RequestPart("audio") MultipartFile audio)
            throws Exception {
        logger.info("Received audio file: " + audio.getOriginalFilename() + ", size: " + audio.getSize() + " bytes");
        byte[] audioBytes = audio.getBytes();

        // Transcribe audio to text
        String transcript = sttService.transcribe(audioBytes);

        // Process the transcribed text with LLM
        Map<String, String> llmResponse = llmService
                .processPrompt(LLMPromptRequest.builder().prompt(transcript).build());

        // Remove markdown bold formatting if present
        String output = llmResponse.get("output").replace("**", "");

        return ResponseEntity.ok(Map.of("output", output));
    }
}
