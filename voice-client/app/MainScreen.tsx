import React, { act, useEffect, useState } from "react";
import {
  SafeAreaView,
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  Platform,
  NativeModules,
  PermissionsAndroid,
  NativeEventEmitter,
  ActivityIndicator
} from "react-native";
import { LinearGradient } from "expo-linear-gradient";
import { sendPrompt, sendVoice } from "@/api/client";
import { useCommandStatus } from "@/state/CommandStatusContext";

const { WakeControl } = NativeModules;

export default function MainScreen() {
  const [prompt, setPrompt] = useState("");
  const [provider, setProvider] = useState<"ollama" | "openai">("ollama");
  const [ttsEnabled, setTtsEnabled] = useState(true);
  const [outputTarget, setOutputTarget] = useState<"phone" | "home">("phone");
  const [lastResult, setLastResult] = useState<string | null>(null);

  const [wakeWordDetectionActive, setWakeWordDetectionActive] = useState(false);

  const { isBusy, startCommand, endCommand } = useCommandStatus();

  useEffect(() => {
    if (!WakeControl || Platform.OS !== "android") return;

    const emitter = new NativeEventEmitter(WakeControl);
    const sub = emitter.addListener("wake_utterance_ready", async (event) => {

      console.log("Received utterance ready event", event);

      const { path, mime } = event;
      if (!path) return;

      if (isBusy) {
        // you can discard or queue
        return;
      }

      startCommand();
      try {
        const fd = new FormData();
        fd.append("audio", {
          uri: "file://" + path,
          name: "utterance.wav",
          type: mime || "audio/wav",
        } as any);

        const data = await sendVoice(fd, { provider });
        setLastResult(data.output ?? JSON.stringify(data));
      } catch (e) {
        console.warn("upload failed", e);
      } finally {
        endCommand();
      }
    });

    return () => {
      sub.remove();
    };
  }, [isBusy, startCommand, endCommand]);

  async function handleSendText() {
    const text = prompt.trim();
    if (!text) return;
    if (isBusy) return; // block if already in-flight

    startCommand();
    setPrompt("");
    try {
      const data = await sendPrompt(text, { provider });
      setLastResult(data.output ?? JSON.stringify(data));
      console.log("LLM:", data);
    } catch (e) {
      console.warn(e);
    } finally {
      endCommand();
    }
  }

  async function ensureMic() {
    if (Platform.OS !== "android") return true;
    const res = await PermissionsAndroid.request(
      PermissionsAndroid.PERMISSIONS.RECORD_AUDIO
    );
    return res === PermissionsAndroid.RESULTS.GRANTED;
  }

  const handleStartStopWake = async () => {
    if (wakeWordDetectionActive) {
      console.log("Stopping wake word detection");
      WakeControl.stop();
      setWakeWordDetectionActive(false);
      return;
    }

    setWakeWordDetectionActive(true);
    const ok = await ensureMic();
    if (!ok) return;
    console.log("Starting wake word detection");
    WakeControl.start();
  };

  return (
    <LinearGradient
      colors={["#0f172a", "#0f172a", "#111827"]}
      start={{ x: 0, y: 0 }}
      end={{ x: 1, y: 1 }}
      style={styles.container}
    >
      <SafeAreaView style={styles.safe}>
        {/* center content */}
        <View style={styles.centerArea}>
          <Text style={styles.greeting}>Hello, Vlad 👋</Text>
          <Text style={styles.subtitle}>What should I do?</Text>

          <View style={styles.orbWrapper}>
            <LinearGradient
              colors={["#4c6fff", "#7c3aed"]}
              start={{ x: 0, y: 0 }}
              end={{ x: 1, y: 1 }}
              style={styles.orb}
            >
              <View style={styles.orbFace}>
                <View style={styles.eye} />
                <View style={styles.eye} />
              </View>
            </LinearGradient>
          </View>

          <View style={styles.quickActionsRow}>
            <ActionPill label="Scene: cozy" onPress={() => setPrompt("Scene: cozy")} />
            <ActionPill label="Lights off" onPress={() => setPrompt("Lights off")} />
          </View>
          <View style={styles.quickActionsRow}>
            <ActionPill label="Play Spotify" onPress={() => setPrompt("Play Spotify")} />
            <ActionPill label="Status report" onPress={() => setPrompt("Status report")} />
          </View>
        </View>

        <View style={{ alignItems: "center", justifyContent: "center", marginBottom: 100 }}>
          {isBusy ? (
            <ActivityIndicator size="large" color="#ffffff" />
          ) : lastResult ? (
            <Text style={{ color: "#fff", textAlign: "center", fontSize: 20 }}>{lastResult}</Text>
          ) : null}
        </View>

        {/* controls + input */}
        <View style={styles.controlsArea}>
          {/* row of labeled segments */}
          <View style={styles.controlRow}>
            <View style={styles.controlGroup}>
              <Text style={styles.controlLabel}>Model</Text>
              <View style={styles.segment}>
                <SegmentButton
                  label="Ollama"
                  active={provider === "ollama"}
                  onPress={() => setProvider("ollama")}
                />
                <SegmentButton
                  label="OpenAI"
                  active={provider === "openai"}
                  onPress={() => setProvider("openai")}
                />
              </View>
            </View>

            <View style={styles.controlGroup}>
              <Text style={styles.controlLabel}>TTS output</Text>
              <View style={styles.segmentSmall}>
                <SegmentButtonSmall
                  label="Phone"
                  active={outputTarget === "phone"}
                  onPress={() => setOutputTarget("phone")}
                />
                <SegmentButtonSmall
                  label="Home speakers"
                  active={outputTarget === "home"}
                  onPress={() => setOutputTarget("home")}
                />
              </View>
            </View>
          </View>

          {/* TTS enable */}
          <View style={styles.controlRow}>
            <View style={styles.ttsRow}>
              <Text style={styles.controlLabel}>TTS</Text>
              <TouchableOpacity
                onPress={() => setTtsEnabled((p) => !p)}
                style={[styles.smallToggle, ttsEnabled && styles.smallToggleOn]}
              >
                <Text style={[styles.smallToggleText, ttsEnabled && styles.smallToggleTextOn]}>
                  {ttsEnabled ? "Enabled" : "Disabled"}
                </Text>
              </TouchableOpacity>
            </View>

            <View style={styles.ttsRow}>
              <Text style={styles.controlLabel}>Wakeword</Text>
              <TouchableOpacity
                style={[styles.smallToggle, wakeWordDetectionActive && styles.smallToggleOn]}
                onPress={handleStartStopWake}
              >
                <Text style={{ color: "#fff" }}>Enable wake word</Text>
              </TouchableOpacity>
            </View>
          </View>

          {/* bottom input */}
          <View style={styles.bottomBar}>
            <View style={styles.inputContainer}>
              <TextInput
                placeholder="Type a command…"
                placeholderTextColor="rgba(255,255,255,0.35)"
                style={styles.input}
                value={prompt}
                onChangeText={setPrompt}
              />
              <TouchableOpacity style={styles.sendButton}
                onPress={handleSendText}>
                <Text style={styles.sendButtonText}>➤</Text>
              </TouchableOpacity>
            </View>

            <TouchableOpacity style={styles.micButton}>
              <Text style={styles.micIcon}>🎤</Text>
            </TouchableOpacity>
          </View>
        </View>
      </SafeAreaView>
    </LinearGradient>
  );
}

function SegmentButton({
  label,
  active,
  onPress,
}: {
  label: string;
  active: boolean;
  onPress: () => void;
}) {
  return (
    <TouchableOpacity
      onPress={onPress}
      style={[styles.segmentButton, active && styles.segmentButtonActive]}
    >
      <Text style={[styles.segmentText, active && styles.segmentTextActive]}>{label}</Text>
    </TouchableOpacity>
  );
}

function SegmentButtonSmall({
  label,
  active,
  onPress,
}: {
  label: string;
  active: boolean;
  onPress: () => void;
}) {
  return (
    <TouchableOpacity
      onPress={onPress}
      style={[styles.segmentButtonSmall, active && styles.segmentButtonActive]}
    >
      <Text style={[styles.segmentTextSmall, active && styles.segmentTextActive]}>{label}</Text>
    </TouchableOpacity>
  );
}

function ActionPill({ label, onPress }: { label: string; onPress: () => void }) {
  return (
    <TouchableOpacity style={styles.actionPill} onPress={onPress}>
      <Text style={styles.actionPillText}>{label}</Text>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  safe: { flex: 1, paddingHorizontal: 20 },
  centerArea: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
  },
  greeting: {
    color: "#ffffff",
    fontSize: 21,
    fontWeight: "600",
  },
  subtitle: {
    color: "rgba(255,255,255,0.45)",
    fontSize: 14,
    marginTop: 4,
    marginBottom: 20,
  },
  orbWrapper: {
    width: 180,
    height: 180,
    borderRadius: 999,
    backgroundColor: "rgba(76,111,255,0.08)",
    justifyContent: "center",
    alignItems: "center",
  },
  orb: {
    width: 140,
    height: 140,
    borderRadius: 999,
    justifyContent: "center",
    alignItems: "center",
  },
  orbFace: {
    flexDirection: "row",
    gap: 16,
  },
  eye: {
    width: 14,
    height: 26,
    borderRadius: 10,
    backgroundColor: "#0f172a",
  },
  quickActionsRow: {
    flexDirection: "row",
    gap: 10,
    marginTop: 16,
  },
  actionPill: {
    backgroundColor: "rgba(15,23,42,0.65)",
    borderRadius: 999,
    paddingHorizontal: 14,
    paddingVertical: 7,
    borderWidth: 1,
    borderColor: "rgba(255,255,255,0.03)",
  },
  actionPillText: {
    color: "#ffffff",
    fontWeight: "500",
    fontSize: 12,
  },
  controlsArea: {
    marginBottom: Platform.OS === "ios" ? 16 : 14,
  },
  controlRow: {
    flexDirection: "row",
    justifyContent: "space-between",
    gap: 14,
  },
  controlGroup: {
    flex: 1,
  },
  controlLabel: {
    color: "rgba(255,255,255,0.5)",
    marginBottom: 6,
    fontSize: 12,
  },
  segment: {
    flexDirection: "row",
    backgroundColor: "rgba(15,23,42,0.4)",
    borderRadius: 999,
    padding: 3,
    borderWidth: 1,
    borderColor: "rgba(255,255,255,0.04)",
  },
  segmentButton: {
    paddingVertical: 6,
    paddingHorizontal: 14,
    borderRadius: 999,
  },
  segmentButtonActive: {
    backgroundColor: "rgba(255,255,255,0.12)",
  },
  segmentText: {
    color: "rgba(255,255,255,0.6)",
    fontWeight: "500",
  },
  segmentTextActive: {
    color: "#ffffff",
  },
  segmentSmall: {
    flexDirection: "row",
    backgroundColor: "rgba(15,23,42,0.4)",
    borderRadius: 999,
    padding: 3,
    borderWidth: 1,
    borderColor: "rgba(255,255,255,0.04)",
  },
  segmentButtonSmall: {
    paddingVertical: 5,
    paddingHorizontal: 10,
    borderRadius: 999,
  },
  segmentTextSmall: {
    color: "rgba(255,255,255,0.6)",
    fontSize: 12,
  },
  ttsRow: {
    flexDirection: "row",
    alignItems: "center",
    marginTop: 10,
    gap: 10,
  },
  smallToggle: {
    backgroundColor: "rgba(15,23,42,0.4)",
    paddingHorizontal: 14,
    paddingVertical: 6,
    borderRadius: 999,
    borderWidth: 1,
    borderColor: "rgba(255,255,255,0.04)",
  },
  smallToggleOn: {
    backgroundColor: "#4c6fff",
    borderColor: "transparent",
  },
  smallToggleText: {
    color: "rgba(255,255,255,0.6)",
    fontWeight: "500",
  },
  smallToggleTextOn: {
    color: "#ffffff",
  },
  bottomBar: {
    flexDirection: "row",
    alignItems: "center",
    marginTop: 14,
    paddingBottom: Platform.OS === "ios" ? 22 : 16,
  },
  inputContainer: {
    flex: 1,
    backgroundColor: "rgba(15,23,42,0.6)",
    borderRadius: 999,
    flexDirection: "row",
    alignItems: "center",
    paddingHorizontal: 14,
    borderWidth: 1,
    borderColor: "rgba(255,255,255,0.03)",
  },
  input: {
    flex: 1,
    color: "#fff",
    height: 46,
  },
  sendButton: {
    backgroundColor: "#4c6fff",
    width: 32,
    height: 32,
    borderRadius: 999,
    justifyContent: "center",
    alignItems: "center",
    marginLeft: 8,
  },
  sendButtonText: {
    color: "#ffffff",
    fontWeight: "700",
    fontSize: 14,
  },
  micButton: {
    marginLeft: 10,
    backgroundColor: "#4c6fff",
    width: 52,
    height: 52,
    borderRadius: 999,
    justifyContent: "center",
    alignItems: "center",
  },
  micIcon: {
    fontSize: 22,
    color: "#fff",
  },
});

