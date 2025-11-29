package com.grouptaskmanager.auth.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "groups")
@NoArgsConstructor
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(unique = true, nullable = false)
    private String passcode;

    public Group(String passcode) {
        this.passcode = passcode;
    }

    @Override
    public String toString() {
        return "Group: " + this.getPasscode();
    }
}

