package com.calendarapp.rest.dailytask;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestDailyTask {
	private Long id;
	private boolean done;
	private String description;
	private String assignee;
}
