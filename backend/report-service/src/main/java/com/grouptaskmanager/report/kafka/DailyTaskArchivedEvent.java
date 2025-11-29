package com.grouptaskmanager.report.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DailyTaskArchivedEvent {
    private Long dailyTaskId;
    private String description;
    private boolean wasDone;
    private LocalDate taskDate;
    private LocalDate archivedDate;
    private Long groupId;
    private Long assigneeUserId;
}

