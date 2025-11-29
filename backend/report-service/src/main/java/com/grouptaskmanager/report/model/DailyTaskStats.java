package com.grouptaskmanager.report.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DailyTaskStats {
    private Long userId;
    private String username;
    private long completedTasks;
    private long totalTasks;
    private double completionRate;
    private int periodDays;
    private Long regularTasksDone;
    private Long regularTasksNotDone;
    private List<String> regularTasksDoneNames;
    private List<String> regularTasksNotDoneNames;
}

