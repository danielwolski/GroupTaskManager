package com.grouptaskmanager.repository;

import com.grouptaskmanager.model.DailyTaskArchive;
import com.grouptaskmanager.model.Group;
import com.grouptaskmanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailyTaskArchiveRepository extends JpaRepository<DailyTaskArchive, Long> {
    
    List<DailyTaskArchive> findByGroup(Group group);
    
    List<DailyTaskArchive> findByGroupAndCreatedBy(Group group, User createdBy);
    
    List<DailyTaskArchive> findByGroupAndTaskDateBetween(Group group, LocalDate startDate, LocalDate endDate);
    
    List<DailyTaskArchive> findByGroupAndCreatedByAndTaskDateBetween(Group group, User createdBy, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT COUNT(dta) FROM DailyTaskArchive dta WHERE dta.group = :group AND dta.createdBy = :user AND dta.wasDone = true AND dta.taskDate >= :startDate")
    Long countCompletedTasksByUserInPeriod(@Param("group") Group group, @Param("user") User user, @Param("startDate") LocalDate startDate);
    
    @Query("SELECT COUNT(dta) FROM DailyTaskArchive dta WHERE dta.group = :group AND dta.createdBy = :user AND dta.taskDate >= :startDate")
    Long countTotalTasksByUserInPeriod(@Param("group") Group group, @Param("user") User user, @Param("startDate") LocalDate startDate);
    
    @Query("SELECT dta.taskDate, COUNT(dta) FROM DailyTaskArchive dta WHERE dta.group = :group AND dta.createdBy = :user AND dta.wasDone = true AND dta.taskDate >= :startDate GROUP BY dta.taskDate ORDER BY dta.taskDate DESC")
    List<Object[]> getDailyCompletionStatsByUser(@Param("group") Group group, @Param("user") User user, @Param("startDate") LocalDate startDate);
}
