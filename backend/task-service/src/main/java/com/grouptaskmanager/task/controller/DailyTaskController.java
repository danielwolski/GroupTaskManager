package com.grouptaskmanager.task.controller;

import com.grouptaskmanager.task.dto.RestCreateDailyTask;
import com.grouptaskmanager.task.dto.RestDailyTask;
import com.grouptaskmanager.task.model.DailyTask;
import com.grouptaskmanager.task.service.DailyTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/daily-tasks")
@RequiredArgsConstructor
public class DailyTaskController {

    private final DailyTaskService dailyTaskService;

    @PostMapping
    public ResponseEntity<DailyTask> createDailyTask(
            @RequestBody RestCreateDailyTask restCreateDailyTask,
            @RequestHeader("X-User-Login") String userLogin) {
        log.info("Received create daily task request from user: {}", userLogin);
        DailyTask createdDailyTask = dailyTaskService.createDailyTask(restCreateDailyTask, userLogin);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(createdDailyTask);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDailyTask(@PathVariable Long id) {
        log.info("Received delete daily task request {}", id);
        dailyTaskService.deleteTask(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<RestDailyTask>> getAllDailyTasksForGroup(
            @RequestHeader("X-User-Login") String userLogin) {
        log.info("Received get all daily tasks request from user: {}", userLogin);
        return ResponseEntity.ok(dailyTaskService.getAllTasksForGroup(userLogin));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> toggleIsDone(@PathVariable Long id) {
        log.info("Received toggle done status for daily task {}", id);
        dailyTaskService.toggleIsDone(id);
        return ResponseEntity.ok().build();
    }
}

