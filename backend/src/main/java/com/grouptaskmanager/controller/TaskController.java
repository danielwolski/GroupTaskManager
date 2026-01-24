package com.grouptaskmanager.controller;

import java.util.List;

import org.apache.catalina.connector.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grouptaskmanager.model.Task;
import com.grouptaskmanager.rest.task.RestCreateTask;
import com.grouptaskmanager.rest.task.RestTask;
import com.grouptaskmanager.service.TaskService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/tasks")
@AllArgsConstructor
public class TaskController {
    
    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody RestCreateTask restCreateTask) {
        log.info("Received create task request {}", restCreateTask.getDescription());
        Task createdTask = taskService.createTask(restCreateTask);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(createdTask);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response> deleteTask(@PathVariable Long id) {
        log.info("Received delete task request {}", id);
        taskService.deleteTask(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<RestTask>> getAllTasksForGroup() {
        return ResponseEntity.ok().body(taskService.getAllTasksForGroup());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> toggleIsDone(@PathVariable Long id) {
        log.info("Received toggle done status for task {}", id);
        taskService.toggleIsDone(id); 
        return ResponseEntity.ok().build();
    }
}
