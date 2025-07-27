package com.home.vlad.servermanager.service.novnc;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.home.vlad.servermanager.dto.novnc.NoVncStatus;
import com.home.vlad.servermanager.exception.novnc.NoVNCAlreadyRunningException;
import com.home.vlad.servermanager.exception.novnc.NoVNCFailedToStartException;
import com.home.vlad.servermanager.exception.novnc.NoVNCNotRunningException;

@Service
public class NoVNCService {
    private static final String VNC_TARGET_URL = "localhost:";

    private final Map<String, Process> processes = new ConcurrentHashMap<>();

    public NoVncStatus getNoVncStatus(String vmName) {
        Process proc = processes.get(vmName);
        return NoVncStatus.builder().vmName(vmName)
                .status((proc != null && proc.isAlive()) ? "running" : "stopped").build();
    }

    public synchronized void start(String vmName, int vmPort, int novncPort) {
        if ("running".equals(getNoVncStatus(vmName).getStatus()))
            throw new NoVNCAlreadyRunningException(vmName);

        if (isPortInUse(novncPort)) {
            System.err.println("Port already in use!");
            throw new NoVNCFailedToStartException(novncPort, vmName);
        }

        ProcessBuilder pb = new ProcessBuilder(
                "/snap/bin/novnc",
                "--listen", Integer.toString(novncPort),
                "--vnc", VNC_TARGET_URL + Integer.toString(vmPort));

        try {
            Process proc = pb.start();
            processes.put(vmName, proc);
        } catch (IOException e) {
            System.err.println("Failed to start process");
            throw new NoVNCFailedToStartException(novncPort, vmName);
        }
    }

    public synchronized void stop(String vmName) {
        Process proc = processes.get(vmName);
        if (proc == null || !proc.isAlive()) {
            throw new NoVNCNotRunningException(vmName);
        }

        ProcessHandle handle = proc.toHandle();
        handle.destroy();

        try {
            boolean exited = handle.onExit()
                    .orTimeout(1, TimeUnit.SECONDS)
                    .handle((p, ex) -> true)
                    .get();

            if (!exited || handle.isAlive()) {
                handle.destroyForcibly();
            }
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error stopping noVNC for " + vmName, e);
        } finally {
            processes.remove(vmName);
        }
    }

    private boolean isPortInUse(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            socket.setReuseAddress(true);
            return false; // able to bind → port is free
        } catch (IOException e) {
            return true; // bind failed → port is in use
        }
    }
}
