package com.calendarapp.repository;

import com.calendarapp.model.Group;
import com.calendarapp.model.Task;

import jakarta.transaction.Transactional;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findAll();

    @Transactional
    @Modifying
    @Query("UPDATE Task t SET t.done = NOT t.done WHERE t.id = :id")
    void toggleIsDone(Long id);

    @Query("SELECT t FROM Task t WHERE t.group = :group")
    List<Task> findAllByGroup(Group group);
}