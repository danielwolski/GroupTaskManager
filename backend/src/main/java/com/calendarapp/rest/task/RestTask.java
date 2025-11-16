package com.calendarapp.rest.task;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestTask {
	private Long id;
	private boolean done;
	private String description;
	private String assignee;
}
