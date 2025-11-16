package com.calendarapp.rest.task;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestCreateTask {
    private String description;
    private Long assigneeUserId;
}
