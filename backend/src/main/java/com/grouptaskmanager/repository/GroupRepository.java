package com.grouptaskmanager.repository;

import com.grouptaskmanager.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findByPasscode(String passcode);
}
