package com.grouptaskmanager.task.repository;

import com.grouptaskmanager.task.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByGroupId(Long groupId);
    List<Task> findAllByAssigneeUserId(Long assigneeUserId);
    List<Task> findAllByGroupIdAndDone(Long groupId, boolean done);
}

