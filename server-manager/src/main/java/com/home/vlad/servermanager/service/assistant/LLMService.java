package com.home.vlad.servermanager.service.assistant;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.CallResponseSpec;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;

import com.home.vlad.servermanager.dto.assistant.LLMPromptRequest;
import com.home.vlad.servermanager.service.NotificationService;

/**
 * LLMService:
 * - Talks to the local Ollama model through Spring AI
 * - Exposes tool calling so the model can call other tools
 */
@Service
public class LLMService {
    private static final Logger logger = LoggerFactory.getLogger(LLMService.class);

    private final ChatClient chatClient;

    private final ToolCallbackProvider toolCallbackProvider;

    private final NotificationService notificationService;

    private final static String SYSTEM_PROMPT = """
            You are Jarvis, an expert personal assistant managing Vlad's home. Be polite, but very brief in your response. \
            Always call the correct tools. If unsure, better to ask for clarification. \
            After a tool call, summarize if it went well or failed. \
            Your response will be read out by a text-to-speech. DO NOT FORMAT YOUR RESPONSE. Plain text only. \
                                    """;

    public LLMService(ChatClient chatClient,
            ToolCallbackProvider toolCallbackProvider,
            NotificationService notificationService) {
        this.chatClient = chatClient;
        this.toolCallbackProvider = toolCallbackProvider;
        this.notificationService = notificationService;
    }

    /**
     * "Preload" the model.
     *
     * We basically issue a trivial request so Ollama spins up/load the model,
     * and we log the fact that it's warmed.
     *
     * We do NOT attach tool schemas here because we don't actually want it
     * to invoke anything during warmup; we just want Ollama to load weights.
     */
    public void preloadModel() {
        logger.info("Preloading model...");

        try {
            CallResponseSpec resp = chatClient
                    .prompt("Ready?")
                    .call();

            logger.info("Model preload response: {}", safeContent(resp));
            logger.info("Model preloaded successfully.");

        } catch (Exception e) {
            logger.error("Error during model preload", e);
            throw new RuntimeException("Error preloading model", e);
        }
    }

    /**
     * Main entry point: given a user prompt, we ask the LLM to respond.
     *
     * IMPORTANT:
     * - We attach the ToolCallbackProvider here. That means the model
     * is allowed to call our @Tool methods (like HomeAssistantScriptTools),
     * and Spring AI will actually invoke them, loop the result back
     * into the model, and produce a final answer.
     */
    public Map<String, String> processPrompt(LLMPromptRequest request) {
        String userPrompt = request.getPrompt();
        logger.info("Processing prompt: {}", userPrompt);

        // Notify: inbound prompt (silent/low priority)
        notificationService.sendSilent("AI Prompt", userPrompt);

        try {
            // This is where Spring AI does the agent loop:
            // 1. Send user prompt + tool schemas to Ollama.
            // 2. If the model requests a tool, Spring AI will:
            // - run that tool via ToolCallbackProvider (in-process Java call)
            // - feed the tool result back to the model
            // 3. Get the final assistant message.

            CallResponseSpec response = chatClient
                    .prompt(userPrompt)
                    .toolCallbacks(toolCallbackProvider)
                    .system(SYSTEM_PROMPT)
                    .call();

            String answer = safeContent(response);

            logger.info("LLM final answer: {}", answer);

            return Map.of("output", answer);

        } catch (Exception e) {
            logger.error("Error while processing prompt", e);
            throw new RuntimeException("LLM processing error", e);
        }
    }

    /**
     * Helper to safely extract the model's final answer text
     * from the CallResponse. If it's null (edge case), fallback
     * to an empty string so we don't explode on Map.of.
     */
    private String safeContent(CallResponseSpec response) {
        if (response == null) {
            return "";
        }
        String content = response.content();
        return (content != null) ? content : "";
    }
}
