package com.home.vlad.servermanager.dto.assistant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TTSRequest {
    private String text;
}
