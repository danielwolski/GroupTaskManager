package com.calendarapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.GenerationType;

import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "dailyTasks")
public class DailyTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private boolean done;
    private String description;
    private LocalDate currentDay = LocalDate.now();

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
        if (currentDay == null) {
            currentDay = LocalDate.now();
        }
    }
}
