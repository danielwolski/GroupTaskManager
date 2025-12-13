package com.grouptaskmanager.task.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DailyTaskEvent {
    private Long dailyTaskId;
    private String description;
    private boolean done;
    private LocalDate taskDate;
    private LocalDate eventDate;
    private Long groupId;
    private Long assigneeUserId;
    private EventType eventType;

    public enum EventType {
        CREATED,
        UPDATED,
        DELETED,
        DAY_RESET
    }
}

