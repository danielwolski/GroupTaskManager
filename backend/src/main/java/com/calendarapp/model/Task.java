package com.calendarapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GenerationType;
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

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "assignee_user_id", nullable = true)
    private User assignee;
}
