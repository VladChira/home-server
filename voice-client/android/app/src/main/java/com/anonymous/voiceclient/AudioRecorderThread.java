package com.anonymous.voiceclient;

import android.Manifest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Process;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.function.BiConsumer;

import com.anonymous.voiceclient.Model;
import com.anonymous.voiceclient.WavUtils;

class AudioRecorderThread extends Thread {
    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final double WAKE_THRESHOLD = 0.1; // your current threshold

    // utterance capture settings
    private static final int UTTER_MAX_MS = 12000; // 12s hard cap
    private static final int SILENCE_MS = 1200; // stop after 1.2s silence
    private static final short SILENCE_SHORT_THRESHOLD = 300; // tune for your mic

    private AudioRecord audioRecord;
    private boolean isRecording = false;

    private volatile boolean busy = false;

    private final Context context;
    ONNXModelRunner modelRunner;
    Model model;
    BiConsumer<String, Long> onUtteranceReady;

    AudioRecorderThread(Context context,
            ONNXModelRunner modelRunner,
            Model model, BiConsumer<String, Long> onUtteranceReady) {
        this.context = context;
        this.modelRunner = modelRunner;
        this.model = model;
        this.onUtteranceReady = onUtteranceReady;
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);

        int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
        int bufferSizeInShorts = 1280;
        if (minBufferSize / 2 < bufferSizeInShorts) {
            minBufferSize = bufferSizeInShorts * 2;
        }

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT,
                minBufferSize);

        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            return;
        }

        short[] audioBuffer = new short[bufferSizeInShorts];
        audioRecord.startRecording();
        isRecording = true;

        boolean capturingUtterance = false;
        long utterStartMs = 0;
        long lastVoiceMs = 0;
        // we'll store utterance in a dynamic array
        java.util.ArrayList<short[]> utterChunks = new java.util.ArrayList<>();

        while (isRecording) {
            int read = audioRecord.read(audioBuffer, 0, audioBuffer.length);
            if (read <= 0)
                continue;

            // wake-word path uses float32
            float[] floatBuffer = new float[read];
            for (int i = 0; i < read; i++) {
                floatBuffer[i] = audioBuffer[i] / 32768.0f;
            }

            String res = model.predict_WakeWord(floatBuffer);

            if (!capturingUtterance) {
                // show wake score
                double score = 0.0;
                try {
                    score = Double.parseDouble(res);
                } catch (Exception ignored) {
                }
                double finalScore = score;

                if (score > WAKE_THRESHOLD) {

                    Log.i("AudioRecorderThread", "[WakeWordDetection] Wake word detected, score: " + finalScore);
                    // start capturing utterance
                    capturingUtterance = true;
                    utterChunks.clear();
                    utterStartMs = System.currentTimeMillis();
                    lastVoiceMs = utterStartMs;
                }

            } else {
                // already in utterance capture
                utterChunks.add(audioBuffer.clone());

                long now = System.currentTimeMillis();

                // simple voice/silence check
                boolean voiced = isVoiced(audioBuffer);
                if (voiced) {
                    lastVoiceMs = now;
                }

                boolean longSilence = (now - lastVoiceMs) > SILENCE_MS;
                boolean tooLong = (now - utterStartMs) > UTTER_MAX_MS;

                if (longSilence || tooLong) {
                    // stop capture, send
                    Log.i("AudioRecorderThread", "[WakeWordDetection] Utterance done, stopping capture");
                    capturingUtterance = false;
                    storeUtterance(utterChunks, System.currentTimeMillis() - utterStartMs);
                }
            }
        }

        releaseResources();
    }

    private boolean isVoiced(short[] buffer) {
        // very dumb RMS-ish gate
        long sum = 0;
        for (short s : buffer) {
            sum += Math.abs(s);
        }
        long avg = sum / buffer.length;
        return avg > SILENCE_SHORT_THRESHOLD;
    }

    private void storeUtterance(java.util.ArrayList<short[]> utterChunks, long durationMs) {
        try {
            String path = WavUtils.writeUtteranceToCache(context, utterChunks, SAMPLE_RATE);
            Log.i("AudioRecorderThread", "[WakeWordDetection] Utterance saved to " + path);

            if (onUtteranceReady != null) {
                onUtteranceReady.accept(path, durationMs);
            }
        } catch (IOException e) {
            Log.e("AudioRecorderThread", "[WakeWordDetection] Failed to write wav", e);
        } finally {
            utterChunks.clear();
        }
    }

    public void stopRecording() {
        isRecording = false;
    }

    private void releaseResources() {
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
    }
}