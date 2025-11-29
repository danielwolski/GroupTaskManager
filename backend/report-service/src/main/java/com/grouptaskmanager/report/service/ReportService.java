package com.grouptaskmanager.report.service;

import com.grouptaskmanager.report.model.DailyTaskEntry;
import com.grouptaskmanager.report.model.DailyTaskStats;
import com.grouptaskmanager.report.model.UserDto;
import com.grouptaskmanager.report.repository.DailyTaskEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final DailyTaskEntryRepository dailyTaskEntryRepository;
    private final AuthServiceClient authServiceClient;

    public DailyTaskStats getCurrentUserStats(String userLogin, int daysBack) {
        UserDto currentUser = authServiceClient.getUserByLogin(userLogin);
        return getUserStats(currentUser.getId(), currentUser.getUsername(), daysBack);
    }

    public List<DailyTaskStats> getAllUsersStats(String userLogin, int daysBack) {
        UserDto currentUser = authServiceClient.getUserByLogin(userLogin);
        
        if (currentUser.getGroupId() == null) {
            throw new RuntimeException("User is not in any group");
        }

        List<UserDto> groupUsers = authServiceClient.getUsersByGroupId(currentUser.getGroupId());
        
        return groupUsers.stream()
                .map(user -> getUserStats(user.getId(), user.getUsername(), daysBack))
                .collect(Collectors.toList());
    }

    private DailyTaskStats getUserStats(Long userId, String username, int daysBack) {
        LocalDate startDate = LocalDate.now().minusDays(daysBack);
        
        long completedTasks = dailyTaskEntryRepository.countCompletedByUserSince(userId, startDate);
        long totalTasks = dailyTaskEntryRepository.countTotalByUserSince(userId, startDate);
        
        double completionRate = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0;

        List<DailyTaskEntry> entries = dailyTaskEntryRepository.findAllByAssigneeUserIdAndDateBetween(
                userId, startDate, LocalDate.now());

        List<String> doneNames = entries.stream()
                .filter(DailyTaskEntry::getWasDone)
                .map(DailyTaskEntry::getDescription)
                .collect(Collectors.toList());

        List<String> notDoneNames = entries.stream()
                .filter(e -> !e.getWasDone())
                .map(DailyTaskEntry::getDescription)
                .collect(Collectors.toList());

        return DailyTaskStats.builder()
                .userId(userId)
                .username(username)
                .completedTasks(completedTasks)
                .totalTasks(totalTasks)
                .completionRate(completionRate)
                .periodDays(daysBack)
                .regularTasksDone((long) doneNames.size())
                .regularTasksNotDone((long) notDoneNames.size())
                .regularTasksDoneNames(doneNames)
                .regularTasksNotDoneNames(notDoneNames)
                .build();
    }
}

