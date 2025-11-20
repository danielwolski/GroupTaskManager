package com.grouptaskmanager.rest.dailytask;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestCreateDailyTask {
    private String description;
    private Long assigneeUserId;
}
