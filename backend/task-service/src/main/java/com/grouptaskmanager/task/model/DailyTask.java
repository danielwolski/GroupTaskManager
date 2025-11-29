package com.grouptaskmanager.task.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "daily_tasks")
public class DailyTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private boolean done;
    
    private String description;
    
    @Column(name = "current_day")
    private LocalDate currentDay = LocalDate.now();

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "assignee_user_id")
    private Long assigneeUserId;

    @PrePersist
    protected void onCreate() {
        if (currentDay == null) {
            currentDay = LocalDate.now();
        }
    }
}

