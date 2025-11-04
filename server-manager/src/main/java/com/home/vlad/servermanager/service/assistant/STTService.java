package com.home.vlad.servermanager.service.assistant;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class STTService {
    private static final String model = "base";
    private static final String lang = "en";
    private static final Duration timeout = Duration.ofSeconds(30);

    private static final ExecutorService CLEANUP_EXECUTOR = java.util.concurrent.Executors.newSingleThreadExecutor();

    Logger logger = LoggerFactory.getLogger(STTService.class);

    public String transcribe(byte[] audioBytes) throws IOException {
        logger.info("Transcribing audio of {} bytes", audioBytes.length);

        Path workDir = Files.createTempDirectory("whisper-stt-");
        Path inputFile = workDir.resolve("input_audio");
        inputFile = inputFile.resolveSibling(inputFile.getFileName() + ".m4a");

        try {
            logger.info("Writing audio bytes to {}", inputFile);
            Files.write(inputFile, audioBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            List<String> cmd = new ArrayList<>();
            cmd.add("whisper");
            cmd.add(inputFile.toAbsolutePath().toString());
            cmd.addAll(List.of(
                    "--model", model,
                    "--output_dir", workDir.toAbsolutePath().toString(),
                    "--output_format", "txt",
                    "--verbose", "False"));
            if (lang != null && !lang.isBlank()) {
                cmd.addAll(List.of("--language", lang));
            }
            logger.info("Starting Whisper with command: {}", String.join(" ", cmd));

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            pb.directory(workDir.toFile());

            Process p = pb.start();

            StringBuilder processLog = new StringBuilder();
            Thread gobbler = new Thread(() -> {
                try (BufferedReader r = new BufferedReader(
                        new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = r.readLine()) != null) {
                        processLog.append(line).append('\n');
                    }
                } catch (IOException ignored) {
                }
            }, "whisper-log-gobbler");
            gobbler.setDaemon(true);
            gobbler.start();

            boolean finished = p.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (!finished) {
                p.destroyForcibly();
                throw new IOException("Whisper timed out after " + timeout.toSeconds() + "s");
            }
            int exit = p.exitValue();
            if (exit != 0) {
                throw new IOException(
                        "Whisper exited with code " + exit + ". Output:\n" + truncate(processLog.toString(), 8000));
            }

            // Find the generated .txt file
            // Whisper writes something like <basename>.txt or <basename>.<lang>.txt in
            // output_dir.
            // We look for the newest .txt in workDir.
            logger.info("Whisper process completed successfully, looking for .txt output");
            List<Path> txts;
            try (var stream = Files.list(workDir)) {
                txts = stream
                        .filter(f -> f.getFileName().toString().endsWith(".txt"))
                        .sorted(Comparator.comparingLong(f -> f.toFile().lastModified()))
                        .collect(Collectors.toList());
            }
            if (txts.isEmpty()) {
                throw new IOException("Whisper completed but no .txt transcript was produced. Output:\n" +
                        truncate(processLog.toString(), 8000));
            }
            Path transcriptFile = txts.get(txts.size() - 1);
            String transcript = Files.readString(transcriptFile, StandardCharsets.UTF_8).trim();

            if (transcript.isEmpty()) {
                throw new IOException("Transcript file was empty (" + transcriptFile.getFileName() + ")");
            }

            logger.info("Transcription completed: {}", truncate(transcript, 200));

            return transcript;

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while running Whisper", ie);
        } finally {
            Path dirToDelete = workDir;
            CLEANUP_EXECUTOR.submit(() -> {
                logger.info("Cleaning up working directory {}", dirToDelete);
                try (var stream = Files.walk(dirToDelete)) {
                    stream.sorted(Comparator.reverseOrder()).forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {
                        }
                    });
                } catch (IOException ignored) {
                }
            });
        }
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }
}
