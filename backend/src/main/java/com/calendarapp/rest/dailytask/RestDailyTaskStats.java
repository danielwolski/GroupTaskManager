package com.calendarapp.rest.dailytask;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RestDailyTaskStats {
    private Long userId;
    private String username;
    private Long completedTasks;
    private Long totalTasks;
    private double completionRate;
    private int periodDays;
    
    // Zwykłe taski
    private Long regularTasksDone;
    private Long regularTasksNotDone;
    private List<String> regularTasksDoneNames;
    private List<String> regularTasksNotDoneNames;
}
