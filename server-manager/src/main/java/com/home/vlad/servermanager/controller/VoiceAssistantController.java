package com.home.vlad.servermanager.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.home.vlad.servermanager.dto.assistant.LLMPromptRequest;
import com.home.vlad.servermanager.dto.assistant.TTSRequest;
import com.home.vlad.servermanager.service.assistant.LLMService;
import com.home.vlad.servermanager.service.assistant.STTService;
import com.home.vlad.servermanager.service.assistant.TTSService;

@RestController
@RequestMapping("/manage/api/v1/voice")
public class VoiceAssistantController {
    Logger logger = LoggerFactory.getLogger(VoiceAssistantController.class);

    private final LLMService llmService;

    private final STTService sttService;

    private final TTSService ttsService;

    public VoiceAssistantController(LLMService llmService, STTService sttService, TTSService ttsService) {
        this.llmService = llmService;
        this.sttService = sttService;
        this.ttsService = ttsService;
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

    @PostMapping(path = "/tts")
    public ResponseEntity<byte[]> textToSpeech(@RequestBody TTSRequest request) throws Exception {
        logger.info("Received text for TTS: " + request.getText());

        byte[] audioBytes = ttsService.textToSpeech(request.getText());

        return ResponseEntity
                .ok()
                .header("Content-Type", "audio/wav")
                .header("Content-Disposition", "inline; filename=\"tts.wav\"")
                .body(audioBytes);
    }
}
