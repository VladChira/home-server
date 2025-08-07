package com.home.vlad.servermanager.dto.minecraft;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MinecraftServerResponse {
    private String id;

    private String name;

    private String status;
}
