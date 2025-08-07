package com.home.vlad.servermanager.dto.catalog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeleteDNSIpPortRequest {
    private String ip;

    private int port;

    private String domain;
}
