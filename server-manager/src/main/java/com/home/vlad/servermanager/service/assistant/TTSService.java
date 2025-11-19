package com.home.vlad.servermanager.service.assistant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;

import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TTSService {

    private static final String VOICE_RESOURCE = "onnx/jarvis-high.onnx";

    private File cachedModelFile;

    @PostConstruct
    public void init() throws IOException {
        // extract once
        this.cachedModelFile = extractModelToTemp();
    }

    @PreDestroy
    public void cleanup() {
        if (cachedModelFile != null && cachedModelFile.exists()) {
            // best effort
            cachedModelFile.delete();
        }
    }

    public byte[] textToSpeech(String text) throws Exception {
        if (cachedModelFile == null || !cachedModelFile.exists()) {
            // fallback: re-extract if someone deleted it
            this.cachedModelFile = extractModelToTemp();
        }

        File wavOut = File.createTempFile("piper-out-", ".wav");

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "piper",
                    "--model", "/home/vlad/services/server-manager/src/main/resources/onnx/jarvis-high.onnx",
                    "--output_file", wavOut.getAbsolutePath());
            pb.redirectErrorStream(true);

            log.info("Starting piper with command " + String.join(" ", pb.command()));

            Process process = pb.start();

            // send text to piper stdin
            try (var writer = new OutputStreamWriter(process.getOutputStream())) {
                writer.write(text);
                writer.flush();
            }

            // drain logs
            try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                while (reader.readLine() != null) {
                    // optionally log
                }
            }

            int exit = process.waitFor();
            if (exit != 0) {
                throw new RuntimeException("piper failed with exit code " + exit);
            }

            return Files.readAllBytes(wavOut.toPath());

        } finally {
            try {
                Files.deleteIfExists(wavOut.toPath());
            } catch (IOException ignored) {
            }
        }
    }

    private File extractModelToTemp() throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(VOICE_RESOURCE)) {
            if (is == null) {
                throw new FileNotFoundException("Voice model resource not found: " + VOICE_RESOURCE);
            }
            File tmp = File.createTempFile("piper-model-", ".onnx");
            Files.copy(is, tmp.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return tmp;
        }
    }
}
