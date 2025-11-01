package com.home.vlad.servermanager.tools;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import com.home.vlad.servermanager.model.tools.TaskSchedule;
import com.home.vlad.servermanager.service.taskscheduler.TaskSchedulerService;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class TaskSchedulingTools {

    private final TaskSchedulerService taskSchedulerService;

    /**
     * Ask the assistant to perform some action in the future instead of right now.
     *
     * REQUIRED FIELDS:
     * - taskSummary: short, high-level description of what should eventually
     * happen.
     * Example: "Turn off all lights in the house."
     *
     * - executionInstructions: step-by-step instructions for how to safely perform
     * the task when it actually runs.
     * Example: "1. Check current light status using tools.
     * 2. If any lights are still on, turn them off.
     * 3. If they're already off, do nothing.
     * 4. Explain what you did."
     *
     * - delayMinutes: how long from now to run it.
     *
     * OPTIONAL FIELDS:
     * - userContext: why this was asked (ex: "User is going to bed.")
     * - requiresManualApproval: true if this should NOT auto-execute when due.
     * The executor will skip it until a human approves.
     *
     * RETURNS:
     * A short confirmation string the assistant can tell the user,
     * like "Okay, I'll handle that in 30 minutes at 02:15."
     */
    @Tool(name = "schedule_delayed_task", description = "Schedule a future action instead of doing it now. " +
            "Use this when the user asks you to do something after a delay. " +
            "Fields: taskSummary (what to do), executionInstructions, " +
            "userContext (why), delayMinutes (when) " +
            "This tool DOES NOT perform the action now.")
    public String scheduleDelayedTask(String taskSummary, String executionInstructions, String userContext,
            Integer delayMinutes) {
        log.info("scheduleDelayedTask called");

        ScheduleDelayedTaskRequest request = ScheduleDelayedTaskRequest.builder()
                .taskSummary(taskSummary)
                .executionInstructions(executionInstructions)
                .userContext(userContext)
                .delayMinutes(delayMinutes)
                .requiresManualApproval(false)
                .build();

        // basic input validation for sanity
        if (request == null) {
            return "Task not scheduled: missing request.";
        }
        if (request.getTaskSummary() == null || request.getTaskSummary().isBlank()) {
            return "Task not scheduled: taskSummary is required.";
        }
        if (request.getExecutionInstructions() == null || request.getExecutionInstructions().isBlank()) {
            return "Task not scheduled: executionInstructions is required.";
        }
        if (request.getDelayMinutes() == null || request.getDelayMinutes() < 1) {
            return "Task not scheduled: delayMinutes must be >= 1.";
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime runAt = now.plusMinutes(request.getDelayMinutes());

        boolean manualApproval = request.getRequiresManualApproval() != null
                && request.getRequiresManualApproval();

        // scheduledBy is "assistant" because the model is invoking this.
        TaskSchedule saved = taskSchedulerService.scheduleTask(
                runAt,
                request.getTaskSummary(),
                request.getExecutionInstructions(),
                request.getUserContext(),
                "AI assistant",
                manualApproval);

        log.info("Assistant scheduled task {} to run at {}", saved.getId(), saved.getRunAt());

        // Short message for the model to present to the user.
        // Keep this lightweight so it doesn't eat tokens.
        return "Scheduled. It will be handled it at "
                + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(saved.getRunAt()) + ". No need to tell the user this exact time.";
    }

    /**
     * Input schema for schedule_delayed_task.
     *
     * The LLM will fill this object when calling the tool.
     */
    @Data
    @Builder
    public static class ScheduleDelayedTaskRequest {
        // "Turn off all lights in the house."
        private String taskSummary;

        // Multi-step guidance for future execution.
        // "1. Check current light status using tools.
        // 2. If any lights are still on, turn them off.
        // 3. If everything is already off, do nothing.
        // 4. Explain what you did."
        private String executionInstructions;

        // "User asked this before going to bed."
        private String userContext;

        // how long from now, in minutes
        private Integer delayMinutes;

        // true = do not auto-run when due; requires manual approval later
        private Boolean requiresManualApproval;
    }
}