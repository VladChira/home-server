package com.home.vlad.servermanager.service.taskscheduler;

import com.home.vlad.servermanager.model.tools.TaskSchedule;
import com.home.vlad.servermanager.repository.tools.TaskScheduleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskSchedulerService {

    private final TaskScheduleRepository repo;

    /**
     * Create a scheduled task entry in the DB.
     *
     * @param runAt                  local time when it should run
     * @param taskSummary            short description like
     *                               "Turn off all lights in the house."
     * @param executionInstructions  detailed model instructions, ex:
     *                               "1. Check current light status using tools.
     *                               2. If any lights are still on, turn them off.
     *                               3. If all lights are already off, do nothing.
     *                               4. Report what you did."
     * @param userContext            human context like
     *                               "User asked this before going to bed."
     * @param scheduledBy            "user" or "assistant"
     * @param requiresManualApproval if true, executor will NOT run automatically
     */
    public TaskSchedule scheduleTask(
            LocalDateTime runAt,
            String taskSummary,
            String executionInstructions,
            String userContext,
            String scheduledBy,
            boolean requiresManualApproval) {
        LocalDateTime now = LocalDateTime.now();

        if (runAt.isBefore(now)) {
            throw new IllegalArgumentException("runAt is in the past");
        }

        TaskSchedule task = TaskSchedule.builder()
                .runAt(runAt)
                .taskSummary(taskSummary)
                .executionInstructions(executionInstructions)
                .userContext(userContext)
                .scheduledBy(scheduledBy)
                .status("scheduled")
                .createdAt(now)
                .executedAt(null)
                .requiresManualApproval(requiresManualApproval)
                .executorResult(null)
                .build();

        TaskSchedule saved = repo.save(task);

        log.info(
                "Scheduled task id={} runAt={} by={} manualApproval={} summary={}",
                saved.getId(),
                saved.getRunAt(),
                saved.getScheduledBy(),
                saved.isRequiresManualApproval(),
                saved.getTaskSummary());

        return saved;
    }
}
