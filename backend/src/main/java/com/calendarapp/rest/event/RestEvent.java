package com.calendarapp.rest.event;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestEvent {
	private Long id;
	private String title;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private String createdBy;
}
