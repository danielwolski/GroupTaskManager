package com.grouptaskmanager.task.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private boolean done;
    
    private String description;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "assignee_user_id")
    private Long assigneeUserId;
}

