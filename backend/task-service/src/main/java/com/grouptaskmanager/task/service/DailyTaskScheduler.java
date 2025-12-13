package com.grouptaskmanager.task.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyTaskScheduler {

    private final DailyTaskService dailyTaskService;

    /**
     * Run on application startup to reset any tasks from previous days
     */
    @PostConstruct
    public void onStartup() {
        log.info("Application started - checking for old daily tasks to reset");
        try {
            dailyTaskService.resetOldDailyTasks();
        } catch (Exception e) {
            log.error("Error resetting old daily tasks on startup", e);
        }
    }

    /**
     * Run every day at midnight to reset daily tasks
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void resetDailyTasks() {
        log.info("Scheduled task - resetting daily tasks for new day");
        try {
            dailyTaskService.resetOldDailyTasks();
        } catch (Exception e) {
            log.error("Error in scheduled daily task reset", e);
        }
    }
}
