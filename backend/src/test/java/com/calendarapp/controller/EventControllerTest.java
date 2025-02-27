package com.calendarapp.controller;

import com.calendarapp.model.Event;
import com.calendarapp.rest.event.RestCreateEvent;
import com.calendarapp.rest.event.RestEvent;
import com.calendarapp.rest.event.RestEventDetails;
import com.calendarapp.service.EventService;
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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class EventControllerTest {

    @Mock
    private EventService eventService;

    @InjectMocks
    private EventController eventController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(eventController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldCreateEvent() throws Exception {
        RestCreateEvent restCreateEvent = new RestCreateEvent();

        Event createdEvent = new Event();
        createdEvent.setId(1L);

        when(eventService.createEvent(any(RestCreateEvent.class))).thenReturn(createdEvent);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(restCreateEvent)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(1));

        verify(eventService, times(1)).createEvent(any(RestCreateEvent.class));
    }

    @Test
    void shouldDeleteEvent() throws Exception {
        Long eventId = 1L;

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/events/{id}", eventId))
                .andExpect(status().isOk());

        verify(eventService, times(1)).deleteEvent(eventId);
    }

    @Test
    void shouldGetAllEventsForGroup() throws Exception {
        RestEvent restEvent = new RestEvent();
        restEvent.setId(1L);

        when(eventService.getAllEventsForGroup()).thenReturn(List.of(restEvent));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/events"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(eventService, times(1)).getAllEventsForGroup();
    }

    @Test
    void shouldGetEventDetails() throws Exception {
        Long eventId = 1L;
        RestEventDetails eventDetails = new RestEventDetails();
        eventDetails.setId(eventId);

        when(eventService.getEventDetails(eventId)).thenReturn(eventDetails);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/events/{id}", eventId))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(eventId));

        verify(eventService, times(1)).getEventDetails(eventId);
    }

    @Test
    void shouldGetEventDetailsByDay() throws Exception {
        LocalDate date = LocalDate.of(2024, 2, 27);
        RestEventDetails eventDetails = new RestEventDetails();
        eventDetails.setId(1L);

        when(eventService.getEventDetailsByDay(date)).thenReturn(List.of(eventDetails));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/events/by-day")
                        .param("date", date.toString()))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(eventService, times(1)).getEventDetailsByDay(date);
    }
}
