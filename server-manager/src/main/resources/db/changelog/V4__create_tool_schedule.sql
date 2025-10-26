CREATE TABLE tool_schedule (
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,

    run_at                      DATETIME NOT NULL,

    task_summary                TEXT NOT NULL,
    execution_instructions      TEXT NOT NULL,
    user_context                TEXT NULL,

    scheduled_by                VARCHAR(64) NOT NULL,

    status                      VARCHAR(32) NOT NULL DEFAULT 'scheduled',

    created_at                  DATETIME NOT NULL,
    executed_at                 DATETIME NULL,

    requires_manual_approval    TINYINT(1) NOT NULL DEFAULT 0,

    executor_result             TEXT NULL
);

CREATE INDEX idx_tool_schedule_status_runat
    ON tool_schedule (status, run_at);

CREATE INDEX idx_tool_schedule_scheduled_by
    ON tool_schedule (scheduled_by);
