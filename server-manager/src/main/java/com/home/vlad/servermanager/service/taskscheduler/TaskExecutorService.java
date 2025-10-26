package com.home.vlad.servermanager.service.taskscheduler;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.home.vlad.servermanager.dto.assistant.LLMPromptRequest;
import com.home.vlad.servermanager.model.tools.TaskSchedule;
import com.home.vlad.servermanager.repository.tools.TaskScheduleRepository;
import com.home.vlad.servermanager.service.assistant.LLMService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskExecutorService {

    private final TaskScheduleRepository repo;
    private final LLMService llmService;

    /**
     * Execute a single scheduled task now.
     * This will:
     * 1. mark status=running
     * 2. build the runtime prompt for the agent
     * 3. call the agent (LLMService)
     * 4. save result, mark completed or skipped
     */
    public void executeTask(TaskSchedule task) {
        Long taskId = task.getId();
        log.info("Executing scheduled task id={} summary='{}'", taskId, task.getTaskSummary());

        // Reload fresh from DB to avoid stale state issues
        task = repo.findById(taskId).orElse(null);
        if (task == null) {
            log.warn("Task id={} disappeared before execution", taskId);
            return;
        }

        // If someone already changed its status (cancelled, etc.), bail.
        if (!"scheduled".equalsIgnoreCase(task.getStatus())) {
            log.info("Task id={} is not scheduled anymore (status={}), skipping.", taskId, task.getStatus());
            return;
        }

        // Mark as running
        task.setStatus("running");
        repo.save(task);

        // Build the LLM instruction prompt
        String runtimePrompt = buildExecutionPrompt(task);

        String assistantResult;

        try {
            // Call your existing agent loop
            var responseMap = llmService.processPrompt(
                    LLMPromptRequest.builder()
                            .prompt(runtimePrompt)
                            .build());

            assistantResult = responseMap.getOrDefault("output", "(no output)");

            log.info("Task id={} executed. Model said: {}", taskId, assistantResult);

        } catch (Exception e) {
            log.error("Task id={} failed to execute cleanly", taskId, e);
            assistantResult = "ERROR during scheduled execution: " + e.getMessage();
        }

        // Finalize task status
        task.setExecutedAt(LocalDateTime.now());
        task.setExecutorResult(assistantResult);
        task.setStatus("completed");
        repo.save(task);
    }

    /**
     * Produce the message we feed back into the LLM when a scheduled task is due.
     * This is the "wake up, do the thing" prompt.
     */
    private String buildExecutionPrompt(TaskSchedule task) {
        String taskSummary = task.getTaskSummary();
        String userContext = task.getUserContext() == null ? "" : task.getUserContext();
        String instructions = task.getExecutionInstructions();

        return "A scheduled action you created earlier is now due.\n\n" +
                "Task:\n" +
                taskSummary + "\n\n" +
                "User context at scheduling time:\n" +
                (userContext.isBlank() ? "No extra context was provided." : userContext) + "\n\n" +
                "Execution instructions:\n" +
                instructions + "\n\n" +
                "Execution policy:\n" +
                "1. First, check the current house state using the available tools (status tools).\n" +
                "2. Only perform actions if they are still appropriate right now.\n" +
                "3. After acting (or choosing not to), briefly explain what you did and why.\n";
    }
}
