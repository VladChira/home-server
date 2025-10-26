package com.home.vlad.servermanager.dto.assistant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LLMPromptRequest {
    private String prompt;
}
