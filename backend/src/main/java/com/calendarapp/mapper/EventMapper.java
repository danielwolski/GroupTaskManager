package com.calendarapp.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.calendarapp.model.Event;
import com.calendarapp.rest.event.RestCreateEvent;
import com.calendarapp.rest.event.RestEvent;
import com.calendarapp.rest.event.RestEventDetails;

@Mapper(componentModel = "spring")
public interface EventMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createdBy", ignore = true)
	@Mapping(target = "group", ignore = true)
	Event restCreateEventToEvent(RestCreateEvent restEvent);
	RestCreateEvent eventToRestCreateEvent(Event event);

	@Mapping(target = "createdBy", source = "createdBy.login")
	RestEvent eventToRestEvent(Event event);
	List<RestEvent> eventListToEventRestList(List<Event> eventList);
	
	@Mapping(target = "createdBy", source = "createdBy.login")
	RestEventDetails eventToEventDetails(Event event);
	List<RestEventDetails> eventListToRestEventDetailsList(List<Event> events);
}
