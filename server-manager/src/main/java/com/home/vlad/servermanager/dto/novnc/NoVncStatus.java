package com.home.vlad.servermanager.dto.novnc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoVncStatus {
    private String vmName;
    private String status;
}
