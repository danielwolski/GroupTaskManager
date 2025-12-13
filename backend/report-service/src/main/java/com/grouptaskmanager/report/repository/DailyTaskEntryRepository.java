package com.grouptaskmanager.report.repository;

import com.grouptaskmanager.report.model.DailyTaskEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyTaskEntryRepository extends JpaRepository<DailyTaskEntry, Long> {
    
    Optional<DailyTaskEntry> findByDailyTaskIdAndDate(Long dailyTaskId, LocalDate date);
    
    List<DailyTaskEntry> findAllByGroupId(Long groupId);
    
    List<DailyTaskEntry> findAllByAssigneeUserId(Long assigneeUserId);
    
    List<DailyTaskEntry> findAllByGroupIdAndDateBetween(Long groupId, LocalDate startDate, LocalDate endDate);
    
    List<DailyTaskEntry> findAllByAssigneeUserIdAndDateBetween(Long assigneeUserId, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT DISTINCT d.assigneeUserId FROM DailyTaskEntry d WHERE d.groupId = :groupId AND d.assigneeUserId IS NOT NULL")
    List<Long> findDistinctAssigneeUserIdsByGroupId(@Param("groupId") Long groupId);
    
    @Query("SELECT COUNT(d) FROM DailyTaskEntry d WHERE d.assigneeUserId = :userId AND d.wasDone = true AND d.date >= :startDate")
    long countCompletedByUserSince(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);
    
    @Query("SELECT COUNT(d) FROM DailyTaskEntry d WHERE d.assigneeUserId = :userId AND d.date >= :startDate")
    long countTotalByUserSince(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);
}

