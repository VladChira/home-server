package com.home.vlad.servermanager.dto.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProvisionDNSRequest {
    @JsonProperty("service_key")
    private String serviceKey;

    private String domain;
}
