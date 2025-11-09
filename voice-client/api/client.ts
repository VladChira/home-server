// src/api/client.ts
const BASE_URL =
    process.env.EXPO_PUBLIC_API_BASE_URL ?? "";
const API_KEY = process.env.EXPO_PUBLIC_API_KEY ?? "";

// helper to build headers
function buildHeaders(provider?: "ollama" | "openai"): HeadersInit {
    const headers: HeadersInit = {
        "Authorization": `Manager-Key ${API_KEY}`,
    };
    if (provider) {
        headers["X-LLM"] = provider;
    }
    return headers;
}

/**
 * POST /manage/api/v1/llm/preload
 * no body
 */
export async function preloadModel(provider?: "ollama" | "openai") {
    const res = await fetch(`${BASE_URL}/manage/api/v1/llm/preload`, {
        method: "POST",
        headers: buildHeaders(provider),
    });

    if (!res.ok) {
        const text = await res.text();
        throw new Error(`preloadModel failed: ${res.status} ${text}`);
    }

    return res.json().catch(() => ({}));
}

/**
 * POST /manage/api/v1/llm/prompt
 * body: { prompt: string }
 * optional header: X-LLM: ollama|openai
 */
export async function sendPrompt(
    prompt: string,
    opts?: { provider?: "ollama" | "openai" }
) {
    const res = await fetch(`${BASE_URL}/manage/api/v1/llm/prompt`, {
        method: "POST",
        headers: {
            ...buildHeaders(opts?.provider),
            "Content-Type": "application/json",
        },
        body: JSON.stringify({ prompt }),
    });

    if (!res.ok) {
        const text = await res.text();
        return `sendPrompt failed: ${res.status} ${text}`;
    }

    return res.json();
}

/**
 * POST /manage/api/v1/voice/command
 * body: multipart/form-data with 'audio'
 *
 * `formData` should already contain the file:
 *   const fd = new FormData();
 *   fd.append("audio", {
 *     uri,
 *     name: "recording.wav",
 *     type: "audio/wav",
 *   } as any);
 */
export async function sendVoice(
    formData: FormData,
    opts?: { provider?: "ollama" | "openai" }
) {
    const res = await fetch(`${BASE_URL}/manage/api/v1/voice/command`, {
        method: "POST",
        headers: {
            ...buildHeaders(opts?.provider),
        },
        body: formData,
    });

    if (!res.ok) {
        const text = await res.text();
        throw new Error(`sendVoice failed: ${res.status} ${text}`);
    }

    return res.json();
}
