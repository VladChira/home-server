package com.home.vlad.servermanager.service.taskscheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.home.vlad.servermanager.model.tools.TaskSchedule;
import com.home.vlad.servermanager.repository.tools.TaskScheduleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class TaskExecutionLoop {

    private final TaskScheduleRepository repo;
    private final TaskExecutorService executorService;

    /**
     * Runs on an interval. You can tune the fixedRate.
     * - fixedRate = how often this method is invoked, in ms.
     * - We keep it pretty low-frequency; nothing in your house needs sub-second
     * timing.
     *
     * Strategy:
     * - Look for tasks that are due now or overdue, 'scheduled', and auto-runnable.
     * - Execute them one by one.
     */
    @Scheduled(fixedRate = 30_000) // every 30 seconds
    public void pollAndExecuteDueTasks() {
        LocalDateTime now = LocalDateTime.now();

        List<TaskSchedule> dueTasks = repo.findRunnable(now);

        if (!dueTasks.isEmpty()) {
            log.info("Found {} due scheduled task(s) at {}", dueTasks.size(), now);
        }

        for (TaskSchedule task : dueTasks) {
            try {
                executorService.executeTask(task);
            } catch (Exception e) {
                // We don't want one bad task to stop the loop
                log.error("Error executing scheduled task id={}", task.getId(), e);
            }
        }
    }
}
