package com.home.vlad.servermanager.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.home.vlad.servermanager.dto.minecraft.MinecraftServerResponse;
import com.home.vlad.servermanager.service.minecraft.MinecraftServerService;

@RestController
@RequestMapping("/manage/api/v1/minecraft")
public class MinecraftController {
    private MinecraftServerService minecraftService;

    public MinecraftController(MinecraftServerService minecraftServerService) {
        this.minecraftService = minecraftServerService;
    }

    @GetMapping
    public List<MinecraftServerResponse> getMinecraftServers() {
        return minecraftService.getMinecraftServers();
    }

    @PostMapping("/{serverName}/start")
    public void startMinecraftServer(@PathVariable String serverName) {
        minecraftService.startMinecraftServer(serverName);
    }

    @PostMapping("/{serverName}/stop")
    public void stopMinecraftServer(@PathVariable String serverName) {
        minecraftService.stopMinecraftServer(serverName);
    }
}
