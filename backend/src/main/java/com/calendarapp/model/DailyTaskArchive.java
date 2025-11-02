package com.calendarapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "daily_task_archives")
public class DailyTaskArchive {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String description;
    private boolean wasDone;
    private LocalDate taskDate;
    private LocalDate archivedDate;
    
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;
    
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "created_by_user_id", nullable = true)
    private User createdBy;
    
    @PrePersist
    protected void onCreate() {
        if (archivedDate == null) {
            archivedDate = LocalDate.now();
        }
    }
}
