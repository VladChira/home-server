package com.home.vlad.servermanager.service.assistant;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

@Component
public class LLMProviderManager {

    // global default
    private final AtomicReference<String> current = new AtomicReference<>("ollama");

    // optional: per-request override (ThreadLocal)
    private final ThreadLocal<String> requestOverride = new ThreadLocal<>();

    private final AtomicReference<Integer> usePreviousChatsGlobal = new AtomicReference<>(0);

    private final ThreadLocal<Integer> previousChatsCount = new ThreadLocal<>();

    public String getCurrentProvider() {
        // header / request override has highest priority
        String ro = requestOverride.get();
        if (ro != null) {
            return ro;
        }
        return current.get();
    }

    public void setGlobalProvider(String provider) {
        current.set(provider);
    }

    public void setRequestOverride(String provider) {
        requestOverride.set(provider);
    }

    public void clearRequestOverride() {
        requestOverride.remove();
    }

    public int getPreviousChatsCount() {
        Integer uro = previousChatsCount.get();
        if (uro != null) {
            return uro.intValue();
        }
        return 0;
    }

    public void setUseMemoryGlobal(Integer useMemoryCount) {
        usePreviousChatsGlobal.set(useMemoryCount);
    }

    public void setPreviousChatsCount(Integer previousChatsCount) {
        this.previousChatsCount.set(previousChatsCount);
    }

    public void clearUseMemoryOverride() {
        previousChatsCount.remove();
    }
}
