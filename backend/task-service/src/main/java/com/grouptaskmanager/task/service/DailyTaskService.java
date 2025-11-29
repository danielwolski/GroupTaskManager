package com.grouptaskmanager.task.service;

import com.grouptaskmanager.task.dto.RestCreateDailyTask;
import com.grouptaskmanager.task.dto.RestDailyTask;
import com.grouptaskmanager.task.dto.UserDto;
import com.grouptaskmanager.task.kafka.DailyTaskArchivedEvent;
import com.grouptaskmanager.task.kafka.DailyTaskEventProducer;
import com.grouptaskmanager.task.model.DailyTask;
import com.grouptaskmanager.task.repository.DailyTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyTaskService {

    private final DailyTaskRepository dailyTaskRepository;
    private final AuthServiceClient authServiceClient;
    private final DailyTaskEventProducer dailyTaskEventProducer;

    public DailyTask createDailyTask(RestCreateDailyTask restCreateDailyTask, String userLogin) {
        UserDto currentUser = authServiceClient.getUserByLogin(userLogin);
        
        if (currentUser.getGroupId() == null) {
            throw new RuntimeException("User is not in any group");
        }

        DailyTask dailyTask = new DailyTask();
        dailyTask.setDescription(restCreateDailyTask.getDescription());
        dailyTask.setDone(false);
        dailyTask.setCurrentDay(LocalDate.now());
        dailyTask.setGroupId(currentUser.getGroupId());
        dailyTask.setAssigneeUserId(restCreateDailyTask.getAssigneeUserId());

        return dailyTaskRepository.save(dailyTask);
    }

    public void deleteTask(Long id) {
        dailyTaskRepository.deleteById(id);
    }

    public List<RestDailyTask> getAllTasksForGroup(String userLogin) {
        UserDto currentUser = authServiceClient.getUserByLogin(userLogin);
        
        if (currentUser.getGroupId() == null) {
            throw new RuntimeException("User is not in any group");
        }

        List<DailyTask> tasks = dailyTaskRepository.findAllByGroupId(currentUser.getGroupId());
        
        return tasks.stream()
                .map(task -> {
                    String assigneeUsername = null;
                    if (task.getAssigneeUserId() != null) {
                        try {
                            UserDto assignee = authServiceClient.getUserById(task.getAssigneeUserId());
                            assigneeUsername = assignee.getUsername();
                        } catch (Exception e) {
                            log.warn("Could not fetch assignee for daily task {}", task.getId());
                        }
                    }
                    return RestDailyTask.builder()
                            .id(task.getId())
                            .done(task.isDone())
                            .description(task.getDescription())
                            .currentDay(task.getCurrentDay())
                            .assigneeUserId(task.getAssigneeUserId())
                            .assigneeUsername(assigneeUsername)
                            .build();
                })
                .collect(Collectors.toList());
    }

    public void toggleIsDone(Long id) {
        DailyTask task = dailyTaskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Daily task not found: " + id));
        task.setDone(!task.isDone());
        dailyTaskRepository.save(task);
    }

    /**
     * Archives old daily tasks and sends events to Report Service via Kafka
     */
    public void archiveOldDailyTasks() {
        LocalDate today = LocalDate.now();
        List<DailyTask> oldTasks = dailyTaskRepository.findAllByCurrentDayBefore(today);
        
        log.info("Found {} daily tasks to archive", oldTasks.size());
        
        for (DailyTask task : oldTasks) {
            // Send event to Kafka for Report Service
            DailyTaskArchivedEvent event = DailyTaskArchivedEvent.builder()
                    .dailyTaskId(task.getId())
                    .description(task.getDescription())
                    .wasDone(task.isDone())
                    .taskDate(task.getCurrentDay())
                    .archivedDate(today)
                    .groupId(task.getGroupId())
                    .assigneeUserId(task.getAssigneeUserId())
                    .build();
            
            dailyTaskEventProducer.sendDailyTaskArchivedEvent(event);
            
            // Reset task for new day
            task.setDone(false);
            task.setCurrentDay(today);
            dailyTaskRepository.save(task);
        }
        
        log.info("Archived {} daily tasks", oldTasks.size());
    }
}

