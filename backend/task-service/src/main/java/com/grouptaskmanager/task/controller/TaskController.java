package com.grouptaskmanager.task.controller;

import com.grouptaskmanager.task.dto.RestCreateTask;
import com.grouptaskmanager.task.dto.RestTask;
import com.grouptaskmanager.task.model.Task;
import com.grouptaskmanager.task.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<Task> createTask(
            @RequestBody RestCreateTask restCreateTask,
            @RequestHeader("X-User-Login") String userLogin) {
        log.info("Received create task request from user: {}", userLogin);
        Task createdTask = taskService.createTask(restCreateTask, userLogin);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(createdTask);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        log.info("Received delete task request {}", id);
        taskService.deleteTask(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<RestTask>> getAllTasksForGroup(
            @RequestHeader("X-User-Login") String userLogin) {
        log.info("Received get all tasks request from user: {}", userLogin);
        return ResponseEntity.ok(taskService.getAllTasksForGroup(userLogin));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> toggleIsDone(@PathVariable Long id) {
        log.info("Received toggle done status for task {}", id);
        taskService.toggleIsDone(id);
        return ResponseEntity.ok().build();
    }
}

