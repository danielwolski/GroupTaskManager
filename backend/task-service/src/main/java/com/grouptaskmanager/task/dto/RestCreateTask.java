package com.grouptaskmanager.task.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RestCreateTask {
    private String description;
    private Long assigneeUserId;
}

