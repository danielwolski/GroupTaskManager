package com.grouptaskmanager.task.service;

import com.grouptaskmanager.task.dto.RestCreateTask;
import com.grouptaskmanager.task.dto.RestTask;
import com.grouptaskmanager.task.dto.UserDto;
import com.grouptaskmanager.task.model.Task;
import com.grouptaskmanager.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final AuthServiceClient authServiceClient;

    public Task createTask(RestCreateTask restCreateTask, String userLogin) {
        UserDto currentUser = authServiceClient.getUserByLogin(userLogin);
        
        if (currentUser.getGroupId() == null) {
            throw new RuntimeException("User is not in any group");
        }

        Task task = new Task();
        task.setDescription(restCreateTask.getDescription());
        task.setDone(false);
        task.setGroupId(currentUser.getGroupId());
        task.setAssigneeUserId(restCreateTask.getAssigneeUserId());

        return taskRepository.save(task);
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    public List<RestTask> getAllTasksForGroup(String userLogin) {
        UserDto currentUser = authServiceClient.getUserByLogin(userLogin);
        
        if (currentUser.getGroupId() == null) {
            throw new RuntimeException("User is not in any group");
        }

        List<Task> tasks = taskRepository.findAllByGroupId(currentUser.getGroupId());
        
        return tasks.stream()
                .map(task -> {
                    String assigneeUsername = null;
                    if (task.getAssigneeUserId() != null) {
                        try {
                            UserDto assignee = authServiceClient.getUserById(task.getAssigneeUserId());
                            assigneeUsername = assignee.getUsername();
                        } catch (Exception e) {
                            log.warn("Could not fetch assignee for task {}", task.getId());
                        }
                    }
                    return RestTask.builder()
                            .id(task.getId())
                            .done(task.isDone())
                            .description(task.getDescription())
                            .assigneeUserId(task.getAssigneeUserId())
                            .assigneeUsername(assigneeUsername)
                            .build();
                })
                .collect(Collectors.toList());
    }

    public void toggleIsDone(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found: " + id));
        task.setDone(!task.isDone());
        taskRepository.save(task);
    }
}

