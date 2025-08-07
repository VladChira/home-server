package com.home.vlad.servermanager.service.minecraft;

import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import com.home.vlad.servermanager.dto.minecraft.MinecraftServerResponse;

@Service
public class MinecraftServerService {

    private Logger logger = LoggerFactory.getLogger(MinecraftServerService.class);

    private DockerClientConfig config;

    private DockerHttpClient httpClient;

    public MinecraftServerService() {
        config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();

        httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();
    }

    public List<MinecraftServerResponse> getMinecraftServers() {
        DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);
        List<Container> containers = dockerClient.listContainersCmd().withShowAll(true).exec();
        List<Container> minecraftServers = containers.stream()
                .filter(container -> container.getImage().contains("minecraft-server")).toList();

        return minecraftServers.stream()
                .map(container -> MinecraftServerResponse.builder().id(container.getId().substring(0, 12))
                        .name(container.getNames()[0].substring(1)).status(container.getState()).build())
                .toList();
    }

    public void startMinecraftServer(String serverName) {
        DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);
        List<Container> containers = dockerClient.listContainersCmd().withShowAll(true).exec();
        Container minecraftServer = containers.stream()
                .filter(c -> serverName.equalsIgnoreCase(c.getNames()[0].substring(1)))
                .findFirst().orElseThrow();
        dockerClient.startContainerCmd(minecraftServer.getId()).exec();
    }

    public void stopMinecraftServer(String serverId) {
        DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);
        dockerClient.stopContainerCmd(serverId).exec();
    }
}
