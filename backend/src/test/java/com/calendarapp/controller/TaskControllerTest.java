package com.calendarapp.controller;

import com.calendarapp.model.Task;
import com.calendarapp.rest.task.RestCreateTask;
import com.calendarapp.rest.task.RestTask;
import com.calendarapp.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(taskController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldCreateTask() throws Exception {
        RestCreateTask restCreateTask = new RestCreateTask();
        restCreateTask.setDescription("Test Task");

        Task createdTask = new Task();
        createdTask.setId(1L);
        createdTask.setDescription("Test Task");

        when(taskService.createTask(any(RestCreateTask.class))).thenReturn(createdTask);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(restCreateTask)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Test Task"));

        verify(taskService, times(1)).createTask(any(RestCreateTask.class));
    }

    @Test
    void shouldDeleteTask() throws Exception {
        Long taskId = 1L;

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/tasks/{id}", taskId))
                .andExpect(status().isOk());

        verify(taskService, times(1)).deleteTask(taskId);
    }

    @Test
    void shouldGetAllTasksForGroup() throws Exception {
        RestTask restTask = new RestTask();
        restTask.setId(1L);
        restTask.setDescription("Test Task");

        when(taskService.getAllTasksForGroup()).thenReturn(List.of(restTask));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/tasks"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].description").value("Test Task"));

        verify(taskService, times(1)).getAllTasksForGroup();
    }

    @Test
    void shouldToggleIsDone() throws Exception {
        Long taskId = 1L;

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/tasks/{id}", taskId))
                .andExpect(status().isOk());

        verify(taskService, times(1)).toggleIsDone(taskId);
    }
}
