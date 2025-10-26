package com.home.vlad.servermanager.model.tools;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tool_schedule")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "run_at", nullable = false)
    private LocalDateTime runAt;

    // short, human-readable "what to do"
    @Column(name = "task_summary", nullable = false, columnDefinition = "TEXT")
    private String taskSummary;

    // detailed instructions for the model when executing
    @Column(name = "execution_instructions", nullable = false, columnDefinition = "TEXT")
    private String executionInstructions;

    // optional why/context from when it was scheduled
    @Column(name = "user_context", columnDefinition = "TEXT")
    private String userContext;

    // "user" or "assistant"
    @Column(name = "scheduled_by", nullable = false, length = 64)
    private String scheduledBy;

    // scheduled | running | completed | skipped | cancelled
    @Column(name = "status", nullable = false, length = 32)
    private String status;

    // when it was created (local time)
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // when it actually executed or was decided not to run (local time)
    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    // if true, executor should NOT auto-run it
    @Column(name = "requires_manual_approval", nullable = false)
    private boolean requiresManualApproval;

    // what happened when we executed
    @Column(name = "executor_result", columnDefinition = "TEXT")
    private String executorResult;
}
