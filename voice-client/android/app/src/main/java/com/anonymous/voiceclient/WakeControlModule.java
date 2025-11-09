package com.anonymous.voiceclient;

import android.content.Intent;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.modules.core.DeviceEventManagerModule;

public class WakeControlModule extends ReactContextBaseJavaModule {

    private static ReactApplicationContext reactContext;

    public static final String EVENT_UTTERANCE_READY = "wake_utterance_ready";

    public WakeControlModule(ReactApplicationContext context) {
        super(context);
        reactContext = context;
    }

    @Override
    public String getName() {
        return "WakeControl";
    }

    @ReactMethod
    public void start() {
        Log.i("WakeControlModule", "[WakeWordDetection] Starting wake word detection");
        Intent svc = new Intent(reactContext, WakeService.class);
        ContextCompat.startForegroundService(reactContext, svc);
    }

    @ReactMethod
    public void stop() {
        Log.i("WakeControlModule", "[WakeWordDetection] Stopping wake word detection");
        Intent svc = new Intent(reactContext, WakeService.class);
        reactContext.stopService(svc);
    }

    public static void emitUtteranceReady(String filePath, String mime, long durationMs) {
       if (reactContext == null) {
            Log.w("WakeControlModule", "React context not ready, cannot emit");
            return;
        }
        WritableMap map = Arguments.createMap();
        map.putString("path", filePath);
        map.putString("mime", mime);
        map.putDouble("durationMs", durationMs);

        reactContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit(EVENT_UTTERANCE_READY, map);

        Log.i("WakeControlModule", "Emitted wake_utterance_ready to JS: " + filePath);
    }
}
