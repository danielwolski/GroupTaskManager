package com.calendarapp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
