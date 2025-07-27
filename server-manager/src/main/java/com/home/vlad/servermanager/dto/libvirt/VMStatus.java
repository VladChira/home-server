package com.home.vlad.servermanager.dto.libvirt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VMStatus {
    private String vmName;
    private String state;
}
