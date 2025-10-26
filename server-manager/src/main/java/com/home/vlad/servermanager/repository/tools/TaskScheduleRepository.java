package com.home.vlad.servermanager.repository.tools;

import com.home.vlad.servermanager.model.tools.TaskSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskScheduleRepository extends JpaRepository<TaskSchedule, Long> {

  /**
   * Tasks that are due now or overdue, not run yet,
   * and that are allowed to run automatically.
   */
  @Query("""
      SELECT t
      FROM TaskSchedule t
      WHERE t.status = 'scheduled'
        AND t.requiresManualApproval = false
        AND t.runAt <= :now
      """)
  List<TaskSchedule> findRunnable(LocalDateTime now);

  /**
   * Tasks that are due but require human approval before running.
   */
  @Query("""
      SELECT t
      FROM TaskSchedule t
      WHERE t.status = 'scheduled'
        AND t.requiresManualApproval = true
        AND t.runAt <= :now
      """)
  List<TaskSchedule> findAwaitingApproval(LocalDateTime now);
}
