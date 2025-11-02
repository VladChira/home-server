package com.home.vlad.servermanager.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class LLMConfig {

    private String provider = "ollama";

    @Bean("ollamaChatClient")
    public ChatClient chatClientOllama(OllamaChatModel ollamaChatModel) {
        return ChatClient.builder(ollamaChatModel).build();
    }

    @Bean("openaiChatClient")
    public ChatClient chatClientOpenAI(OpenAiChatModel openAiChatModel) {
        return ChatClient.builder(openAiChatModel).build();
    }

    @Bean
    @Primary
    public ChatClient chatClient(@Qualifier("ollamaChatClient") ChatClient ollamaChatClient,
            @Qualifier("openaiChatClient") ChatClient openAiChatClient) {
        if (provider.equals("openai")) {
            return openAiChatClient;
        } else {
            return ollamaChatClient;
        }
    }

}
