package com.calendarapp.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.calendarapp.model.Event;
import com.calendarapp.rest.RestCreateEvent;
import com.calendarapp.rest.RestEvent;
import com.calendarapp.rest.RestEventDetails;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    Event restCreateEventToEvent(RestCreateEvent restEvent);
    RestCreateEvent eventToRestCreateEvent(Event event);

    Event eventRestToEvent(RestEvent restEvent);

    List<Event> eventRestListToEventList(List<RestEvent> eventRestList);
    List<RestEvent> eventListToEventRestList(List<Event> eventList);
    
    RestEventDetails eventToEventDetails(Event event);
    List<RestEventDetails> eventListToRestEventDetailsList(List<Event> events);
}
