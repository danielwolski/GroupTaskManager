package com.calendarapp.service;

import com.calendarapp.mapper.TaskMapper;
import com.calendarapp.model.Group;
import com.calendarapp.model.Task;
import com.calendarapp.repository.TaskRepository;
import com.calendarapp.rest.task.RestCreateTask;
import com.calendarapp.rest.task.RestTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private TaskService taskService;

    private Task sampleTask;
    private RestCreateTask restCreateTask;
    private Group sampleGroup;

    @BeforeEach
    void setUp() {
        sampleGroup = new Group();
        sampleTask = new Task();
        sampleTask.setId(1L);
        sampleTask.setGroup(sampleGroup);

        restCreateTask = new RestCreateTask();
    }

    @Test
    void shouldCreateTask() {
        when(taskMapper.restCreateTaskToTask(restCreateTask)).thenReturn(sampleTask);
        when(userService.getCurrentUserGroup()).thenReturn(sampleGroup);
        when(taskRepository.save(sampleTask)).thenReturn(sampleTask);

        Task result = taskService.createTask(restCreateTask);

        assertNotNull(result);
        assertEquals(sampleTask, result);
        verify(taskRepository).save(sampleTask);
    }

    @Test
    void shouldGetAllTasksForGroup() {
        List<Task> taskList = List.of(sampleTask);
        List<RestTask> restTaskList = List.of(new RestTask());

        when(userService.getCurrentUserGroup()).thenReturn(sampleGroup);
        when(taskRepository.findAllByGroup(sampleGroup)).thenReturn(taskList);
        when(taskMapper.taskListToTaskRestList(taskList)).thenReturn(restTaskList);

        List<RestTask> result = taskService.getAllTasksForGroup();

        assertNotNull(result);
        assertEquals(restTaskList, result);
        verify(taskRepository).findAllByGroup(sampleGroup);
        verify(taskMapper).taskListToTaskRestList(taskList);
    }

    @Test
    void shouldDeleteTask() {
        Long taskId = 1L;

        taskService.deleteTask(taskId);

        verify(taskRepository, times(1)).deleteById(taskId);
    }

    @Test
    void shouldToggleIsDone() {
        Long taskId = 1L;

        taskService.toggleIsDone(taskId);

        verify(taskRepository, times(1)).toggleIsDone(taskId);
    }
}
