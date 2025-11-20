package com.grouptaskmanager.repository;

import com.grouptaskmanager.model.DailyTask;

import com.grouptaskmanager.model.Group;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DailyTaskRepository extends JpaRepository<DailyTask, Long> {

    List<DailyTask> findAll();

    @Transactional
    @Modifying
    @Query("UPDATE DailyTask t SET t.done = NOT t.done WHERE t.id = :id")
    void toggleIsDone(Long id);

    @Transactional
    @Modifying
    @Query("UPDATE DailyTask t SET t.done = false, t.currentDay = :today")
    void setAllIsDoneToFalse(LocalDate today);

    @Transactional
    @Modifying
    @Query("UPDATE DailyTask t SET t.done = false, t.currentDay = :today WHERE t.currentDay <> :today")
    void setIsDoneToFalseIfNecessary(LocalDate today);

    @Query("SELECT t FROM DailyTask t WHERE t.group = :group")
    List<DailyTask> findAllByGroup(Group group);
}