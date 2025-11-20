package com.grouptaskmanager.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyTaskStats {
    private Long userId;
    private String username;
    private Long completedTasks;
    private Long totalTasks;
    private double completionRate;
    private int periodDays;
    
    private Long regularTasksDone;
    private Long regularTasksNotDone;
    private List<String> regularTasksDoneNames;
    private List<String> regularTasksNotDoneNames;
}
