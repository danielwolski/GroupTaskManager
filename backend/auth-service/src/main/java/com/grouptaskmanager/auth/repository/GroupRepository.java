package com.grouptaskmanager.auth.repository;

import com.grouptaskmanager.auth.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findByPasscode(String passcode);
    boolean existsByPasscode(String passcode);
}

