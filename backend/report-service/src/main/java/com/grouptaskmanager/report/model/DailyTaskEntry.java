package com.grouptaskmanager.report.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "daily_task_entries")
public class DailyTaskEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "daily_task_id", nullable = false)
    private Long dailyTaskId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "was_done", nullable = false)
    private Boolean wasDone;

    private String description;

    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "assignee_user_id")
    private Long assigneeUserId;
}

