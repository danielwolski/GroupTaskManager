package com.grouptaskmanager.service;

import com.grouptaskmanager.mapper.DailyTaskMapper;
import com.grouptaskmanager.model.DailyTask;
import com.grouptaskmanager.model.Group;
import com.grouptaskmanager.repository.DailyTaskRepository;
import com.grouptaskmanager.rest.dailytask.RestCreateDailyTask;
import com.grouptaskmanager.rest.dailytask.RestDailyTask;
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
class DailyTaskServiceTest {

    @Mock
    private DailyTaskRepository dailyTaskRepository;

    @Mock
    private DailyTaskMapper dailyTaskMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private DailyTaskService dailyTaskService;

    private DailyTask sampleTask;
    private RestCreateDailyTask restCreateDailyTask;
    private Group sampleGroup;

    @BeforeEach
    void setUp() {
        sampleGroup = new Group();
        sampleTask = new DailyTask();
        sampleTask.setId(1L);
        sampleTask.setGroup(sampleGroup);

        restCreateDailyTask = new RestCreateDailyTask();
    }

    @Test
    void shouldCreateDailyTask() {
        when(dailyTaskMapper.restCreateDailyTaskToDailyTask(restCreateDailyTask)).thenReturn(sampleTask);
        when(userService.getCurrentUserGroup()).thenReturn(sampleGroup);
        when(dailyTaskRepository.save(sampleTask)).thenReturn(sampleTask);

        DailyTask result = dailyTaskService.createDailyTask(restCreateDailyTask);

        assertNotNull(result);
        assertEquals(sampleTask, result);
        verify(dailyTaskRepository).save(sampleTask);
    }

    @Test
    void shouldGetAllTasksForGroup() {
        List<DailyTask> taskList = List.of(sampleTask);
        List<RestDailyTask> restTaskList = List.of(new RestDailyTask());

        when(userService.getCurrentUserGroup()).thenReturn(sampleGroup);
        when(dailyTaskRepository.findAllByGroup(sampleGroup)).thenReturn(taskList);
        when(dailyTaskMapper.dailyTaskListToDailyTaskRestList(taskList)).thenReturn(restTaskList);

        List<RestDailyTask> result = dailyTaskService.getAllTasksForGroup();

        assertNotNull(result);
        assertEquals(restTaskList, result);
        verify(dailyTaskRepository).findAllByGroup(sampleGroup);
        verify(dailyTaskMapper).dailyTaskListToDailyTaskRestList(taskList);
    }

    @Test
    void shouldDeleteTask() {
        Long taskId = 1L;

        dailyTaskService.deleteTask(taskId);

        verify(dailyTaskRepository, times(1)).deleteById(taskId);
    }

    @Test
    void shouldToggleIsDone() {
        Long taskId = 1L;

        dailyTaskService.toggleIsDone(taskId);

        verify(dailyTaskRepository, times(1)).toggleIsDone(taskId);
    }
}
