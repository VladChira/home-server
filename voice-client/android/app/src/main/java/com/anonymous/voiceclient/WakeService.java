package com.anonymous.voiceclient;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import com.facebook.react.bridge.ReactContext;

public class WakeService extends Service {

    private static final String TAG = "WakeService";
    private AudioRecorderThread recorderThread;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "[WakeWordDetection] onCreate");

        ONNXModelRunner modelRunner;
        Model model;
        try {
            modelRunner = new ONNXModelRunner(getAssets());
            model = new Model(modelRunner);
        } catch (Exception e) {
            Log.e(TAG, "[WakeWordDetection] Failed to init model", e);
            stopSelf();
            return;
        }

        recorderThread = new AudioRecorderThread(this, modelRunner, model, this::onUtteranceReady);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notif = NotificationHelper.buildForegroundNotification(this);
        startForeground(1, notif);

        if (recorderThread != null && !recorderThread.isAlive()) {
            recorderThread.start();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "[WakeWordDetection] onDestroy");
        if (recorderThread != null) {
            recorderThread.stopRecording();
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void onUtteranceReady(String filePath, long durationMs) {
        WakeControlModule.emitUtteranceReady(filePath, "audio/wav", durationMs);
    }
}
