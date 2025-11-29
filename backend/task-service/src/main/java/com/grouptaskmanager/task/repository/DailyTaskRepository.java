package com.grouptaskmanager.task.repository;

import com.grouptaskmanager.task.model.DailyTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailyTaskRepository extends JpaRepository<DailyTask, Long> {
    List<DailyTask> findAllByGroupId(Long groupId);
    List<DailyTask> findAllByAssigneeUserId(Long assigneeUserId);
    List<DailyTask> findAllByCurrentDayBefore(LocalDate date);
    List<DailyTask> findAllByGroupIdAndCurrentDay(Long groupId, LocalDate currentDay);
}

