package com.calendarapp.service;

import com.calendarapp.model.Group;
import com.calendarapp.repository.DailyTaskRepository;
import com.calendarapp.repository.GroupRepository;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class DailyTaskResetter {

    private final DailyTaskRepository dailyTaskRepository;
    private final DailyTaskArchiveService dailyTaskArchiveService;
    private final GroupRepository groupRepository;

    @PostConstruct
    public void resetAtTheStartIfNecessary() {
        LocalDate today = LocalDate.now();
        log.info("Resetting daily tasks if necessary");

        log.info("today: {}", today);
        log.info("existing: {}", dailyTaskRepository.findAll());
        dailyTaskRepository.setIsDoneToFalseIfNecessary(today);
    }

    @Scheduled(cron = "59 59 23 * * *")
    public void resetDailyTasksOnNextDay() {
        LocalDate today = LocalDate.now();
        log.info("Resetting daily tasks: " + today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        try {
            List<Group> allGroups = groupRepository.findAll();
            for (Group group : allGroups) {
                try {
                    dailyTaskArchiveService.archiveDailyTasksForGroup(group);
                    log.info("Successfully archived daily tasks for group: {}", group.getPasscode());
                } catch (Exception e) {
                    log.error("Error archiving daily tasks for group {}: {}", group.getPasscode(), e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("Error during daily tasks archiving process: {}", e.getMessage(), e);
        }

        dailyTaskRepository.setAllIsDoneToFalse(today);
    }
}
