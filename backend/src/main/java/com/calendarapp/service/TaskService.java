package com.calendarapp.service;

import java.util.List;

import com.calendarapp.model.Group;
import org.springframework.stereotype.Service;

import com.calendarapp.mapper.TaskMapper;
import com.calendarapp.model.Task;
import com.calendarapp.repository.TaskRepository;
import com.calendarapp.rest.task.RestCreateTask;

import com.calendarapp.rest.task.RestTask;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class TaskService {
    private final TaskMapper taskMapper;
    private final TaskRepository taskRepository;
    private final UserService userService;

    public Task createTask(RestCreateTask restTask) {
        Task task = taskMapper.restCreateTaskToTask(restTask);
        task.setGroup(userService.getCurrentUserGroup());
        Task savedTask = taskRepository.save(task);
        log.info("Task saved: {}", savedTask);
        return savedTask;
    }

    public List<RestTask> getAllTasksForGroup() {
        Group currentUserGroup = userService.getCurrentUserGroup();
        return taskMapper.taskListToTaskRestList(taskRepository.findAllByGroup(currentUserGroup));
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    public void toggleIsDone(Long id) {
        taskRepository.toggleIsDone(id);
    }
}
