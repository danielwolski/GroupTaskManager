package com.calendarapp.controller;

import com.calendarapp.model.DailyTask;
import com.calendarapp.rest.dailytask.RestCreateDailyTask;
import com.calendarapp.rest.dailytask.RestDailyTask;
import com.calendarapp.service.DailyTaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DailyTaskControllerTest {

    @Mock
    private DailyTaskService dailyTaskService;

    @InjectMocks
    private DailyTaskController dailyTaskController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dailyTaskController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldCreateDailyTask() throws Exception {
        RestCreateDailyTask restCreateDailyTask = new RestCreateDailyTask();
        restCreateDailyTask.setDescription("Test Task");

        DailyTask createdTask = new DailyTask();
        createdTask.setId(1L);
        createdTask.setDescription("Test Task");

        when(dailyTaskService.createDailyTask(any(RestCreateDailyTask.class))).thenReturn(createdTask);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/daily-tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(restCreateDailyTask)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Test Task"));

        verify(dailyTaskService, times(1)).createDailyTask(any(RestCreateDailyTask.class));
    }

    @Test
    void shouldDeleteDailyTask() throws Exception {
        Long taskId = 1L;

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/daily-tasks/{id}", taskId))
                .andExpect(status().isOk());

        verify(dailyTaskService, times(1)).deleteTask(taskId);
    }

    @Test
    void shouldGetAllDailyTasksForGroup() throws Exception {
        RestDailyTask restDailyTask = new RestDailyTask();
        restDailyTask.setId(1L);
        restDailyTask.setDescription("Test Task");

        when(dailyTaskService.getAllTasksForGroup()).thenReturn(List.of(restDailyTask));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/daily-tasks"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].description").value("Test Task"));

        verify(dailyTaskService, times(1)).getAllTasksForGroup();
    }

    @Test
    void shouldToggleIsDone() throws Exception {
        Long taskId = 1L;

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/daily-tasks/{id}", taskId))
                .andExpect(status().isOk());

        verify(dailyTaskService, times(1)).toggleIsDone(taskId);
    }
}
