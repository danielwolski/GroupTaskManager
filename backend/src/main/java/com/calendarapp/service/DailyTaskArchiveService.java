package com.calendarapp.service;

import com.calendarapp.model.DailyTask;
import com.calendarapp.model.DailyTaskArchive;
import com.calendarapp.model.DailyTaskStats;
import com.calendarapp.model.Group;
import com.calendarapp.model.User;
import com.calendarapp.repository.DailyTaskArchiveRepository;
import com.calendarapp.repository.DailyTaskRepository;
import com.calendarapp.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class DailyTaskArchiveService {
    
    private final DailyTaskArchiveRepository dailyTaskArchiveRepository;
    private final DailyTaskRepository dailyTaskRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    
    public void archiveDailyTasksForGroup(Group group) {
        List<DailyTask> dailyTasks = dailyTaskRepository.findAllByGroup(group);
        LocalDate today = LocalDate.now();
        
        List<DailyTaskArchive> archives = dailyTasks.stream()
                .map(dailyTask -> createArchiveFromDailyTask(dailyTask, today))
                .collect(Collectors.toList());
        
        dailyTaskArchiveRepository.saveAll(archives);
        log.info("Archived {} daily tasks for group {}", archives.size(), group.getPasscode());
    }
    
    private DailyTaskArchive createArchiveFromDailyTask(DailyTask dailyTask, LocalDate archiveDate) {
        DailyTaskArchive archive = new DailyTaskArchive();
        archive.setDescription(dailyTask.getDescription());
        archive.setWasDone(dailyTask.isDone());
        archive.setTaskDate(dailyTask.getCurrentDay());
        archive.setArchivedDate(archiveDate);
        archive.setGroup(dailyTask.getGroup());
        archive.setCreatedBy(dailyTask.getCreatedBy());
        return archive;
    }
    
    public DailyTaskStats getUserStats(User user, int daysBack) {
        Group group = userService.getCurrentUserGroup();
        LocalDate startDate = LocalDate.now().minusDays(daysBack);
        
        Long completedTasks = dailyTaskArchiveRepository.countCompletedTasksByUserInPeriod(group, user, startDate);
        Long totalTasks = dailyTaskArchiveRepository.countTotalTasksByUserInPeriod(group, user, startDate);
        
        double completionRate = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0;
        
        return DailyTaskStats.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .completedTasks(completedTasks)
                .totalTasks(totalTasks)
                .completionRate(completionRate)
                .periodDays(daysBack)
                .build();
    }
    
    public List<DailyTaskStats> getAllUsersStats(int daysBack) {
        Group group = userService.getCurrentUserGroup();
        List<User> usersInGroup = userRepository.findByGroup(group);
        
        return usersInGroup.stream()
                .map(user -> getUserStats(user, daysBack))
                .collect(Collectors.toList());
    }
}
