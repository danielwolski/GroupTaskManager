package com.calendarapp.service;

import com.calendarapp.mapper.EventMapper;
import com.calendarapp.model.Event;
import com.calendarapp.model.Group;
import com.calendarapp.repository.EventRepository;
import com.calendarapp.rest.event.RestCreateEvent;
import com.calendarapp.rest.event.RestEvent;
import com.calendarapp.rest.event.RestEventDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventMapper eventMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private EventService eventService;

    private Event sampleEvent;
    private RestCreateEvent restCreateEvent;
    private Group sampleGroup;

    @BeforeEach
    void setUp() {
        sampleGroup = new Group();
        sampleEvent = new Event();
        sampleEvent.setId(1L);
        sampleEvent.setGroup(sampleGroup);

        restCreateEvent = new RestCreateEvent();
    }

    @Test
    void shouldCreateEvent() {
        when(eventMapper.restCreateEventToEvent(restCreateEvent)).thenReturn(sampleEvent);
        when(userService.getCurrentUserGroup()).thenReturn(sampleGroup);
        when(eventRepository.save(sampleEvent)).thenReturn(sampleEvent);

        Event result = eventService.createEvent(restCreateEvent);

        assertNotNull(result);
        assertEquals(sampleEvent, result);
        verify(eventRepository).save(sampleEvent);
    }

    @Test
    void shouldGetAllEventsForGroup() {
        List<Event> eventList = List.of(sampleEvent);
        List<RestEvent> restEventList = List.of(new RestEvent());

        when(userService.getCurrentUserGroup()).thenReturn(sampleGroup);
        when(eventRepository.findAllByGroup(sampleGroup)).thenReturn(eventList);
        when(eventMapper.eventListToEventRestList(eventList)).thenReturn(restEventList);

        List<RestEvent> result = eventService.getAllEventsForGroup();

        assertNotNull(result);
        assertEquals(restEventList, result);
        verify(eventRepository).findAllByGroup(sampleGroup);
        verify(eventMapper).eventListToEventRestList(eventList);
    }

    @Test
    void shouldDeleteEvent() {
        Long eventId = 1L;

        eventService.deleteEvent(eventId);

        verify(eventRepository, times(1)).deleteById(eventId);
    }

    @Test
    void shouldGetEventDetails() {
        Long eventId = 1L;
        RestEventDetails eventDetails = new RestEventDetails();

        when(eventRepository.getById(eventId)).thenReturn(sampleEvent);
        when(eventMapper.eventToEventDetails(sampleEvent)).thenReturn(eventDetails);

        RestEventDetails result = eventService.getEventDetails(eventId);

        assertNotNull(result);
        assertEquals(eventDetails, result);
        verify(eventRepository).getById(eventId);
        verify(eventMapper).eventToEventDetails(sampleEvent);
    }

    @Test
    void shouldGetEventDetailsByDay() {
        LocalDate date = LocalDate.now();
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        List<Event> eventList = List.of(sampleEvent);
        List<RestEventDetails> restEventDetailsList = List.of(new RestEventDetails());

        when(eventRepository.findAllByStartTimeBetween(startOfDay, endOfDay)).thenReturn(eventList);
        when(eventMapper.eventListToRestEventDetailsList(eventList)).thenReturn(restEventDetailsList);

        List<RestEventDetails> result = eventService.getEventDetailsByDay(date);

        assertNotNull(result);
        assertEquals(restEventDetailsList, result);
        verify(eventRepository).findAllByStartTimeBetween(startOfDay, endOfDay);
        verify(eventMapper).eventListToRestEventDetailsList(eventList);
    }
}
