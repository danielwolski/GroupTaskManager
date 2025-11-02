package com.calendarapp.rest.dailytask;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestDailyTaskStats {
    private Long userId;
    private String username;
    private Long completedTasks;
    private Long totalTasks;
    private double completionRate;
    private int periodDays;
}
