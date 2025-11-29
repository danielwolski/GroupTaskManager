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
     * Run on application startup to archive any tasks from previous days
     */
    @PostConstruct
    public void onStartup() {
        log.info("Application started - checking for old daily tasks to archive");
        try {
            dailyTaskService.archiveOldDailyTasks();
        } catch (Exception e) {
            log.error("Error archiving old daily tasks on startup", e);
        }
    }

    /**
     * Run every day at midnight to archive daily tasks
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void archiveDailyTasks() {
        log.info("Scheduled task - archiving daily tasks");
        try {
            dailyTaskService.archiveOldDailyTasks();
        } catch (Exception e) {
            log.error("Error in scheduled daily task archiving", e);
        }
    }
}

