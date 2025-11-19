package com.home.vlad.servermanager.service.assistant;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class ConversationMemoryService {

    private static final int MAX_EXCHANGES = 5;

    private final Deque<Exchange> exchanges = new ArrayDeque<>();

    public synchronized void addExchange(Exchange ex) {
        if (exchanges.size() >= MAX_EXCHANGES) {
            exchanges.removeFirst();
        }
        exchanges.addLast(ex);
    }

    /**
     * Get the last N exchanges, newest last.
     */
    public synchronized List<Exchange> getLast(int n) {
        List<Exchange> list = new ArrayList<>(exchanges);
        int size = list.size();
        if (n >= size) {
            return list;
        }
        return list.subList(size - n, size);
    }

    public static class Exchange {
        private final String userPrompt;
        private final String assistantAnswer;
        private final List<String> toolNames;

        public Exchange(String userPrompt, String assistantAnswer, List<String> toolNames) {
            this.userPrompt = userPrompt;
            this.assistantAnswer = assistantAnswer;
            this.toolNames = toolNames;
        }

        public String getUserPrompt() {
            return userPrompt;
        }

        public String getAssistantAnswer() {
            return assistantAnswer;
        }

        public List<String> getToolNames() {
            return toolNames;
        }
    }
}
