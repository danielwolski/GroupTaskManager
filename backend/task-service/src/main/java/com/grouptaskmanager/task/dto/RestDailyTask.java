package com.grouptaskmanager.task.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RestDailyTask {
    private Long id;
    private boolean done;
    private String description;
    private LocalDate currentDay;
    private Long assigneeUserId;
    private String assigneeUsername;
}

