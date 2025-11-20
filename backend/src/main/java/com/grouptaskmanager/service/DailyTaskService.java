package com.grouptaskmanager.service;

import java.util.List;

import com.grouptaskmanager.mapper.DailyTaskMapper;
import com.grouptaskmanager.mapper.DailyTaskStatsMapper;
import com.grouptaskmanager.model.DailyTaskStats;
import com.grouptaskmanager.model.Group;
import com.grouptaskmanager.model.User;
import com.grouptaskmanager.repository.DailyTaskRepository;
import com.grouptaskmanager.rest.dailytask.RestDailyTask;
import com.grouptaskmanager.rest.dailytask.RestDailyTaskStats;
import org.springframework.stereotype.Service;

import com.grouptaskmanager.model.DailyTask;
import com.grouptaskmanager.rest.dailytask.RestCreateDailyTask;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class DailyTaskService {
	private final DailyTaskMapper dailyTaskMapper;
	private final DailyTaskStatsMapper dailyTaskStatsMapper;
	private final DailyTaskRepository dailyTaskRepository;
	private final DailyTaskArchiveService dailyTaskArchiveService;
	private final UserService userService;

	public DailyTask createDailyTask(RestCreateDailyTask restDailyTask) {
		DailyTask dailyTask = dailyTaskMapper.restCreateDailyTaskToDailyTask(restDailyTask);
		dailyTask.setGroup(userService.getCurrentUserGroup());
		if (restDailyTask.getAssigneeUserId() != null) {
			dailyTask.setAssignee(userService.getUserById(restDailyTask.getAssigneeUserId()));
		}
		DailyTask savedDailyTask = dailyTaskRepository.save(dailyTask);
		log.info("Daily task saved: {}", savedDailyTask);
		return savedDailyTask;
	}

	public List<RestDailyTask> getAllTasksForGroup() {
		Group currentUserGroup = userService.getCurrentUserGroup();
		return dailyTaskMapper.dailyTaskListToDailyTaskRestList(dailyTaskRepository.findAllByGroup(currentUserGroup));
	}

	public void deleteTask(Long id) {
		dailyTaskRepository.deleteById(id);
	}

	public void toggleIsDone(Long id) {
		dailyTaskRepository.toggleIsDone(id);
	}
	
	public RestDailyTaskStats getCurrentUserStats(int daysBack) {
		User currentUser = userService.getCurrentUser();
		DailyTaskStats stats = dailyTaskArchiveService.getUserStats(currentUser, daysBack);
		return dailyTaskStatsMapper.dailyTaskStatsToRestDailyTaskStats(stats);
	}
	
	public List<RestDailyTaskStats> getAllUsersStats(int daysBack) {
		List<DailyTaskStats> stats = dailyTaskArchiveService.getAllUsersStats(daysBack);
		return dailyTaskStatsMapper.dailyTaskStatsListToRestDailyTaskStatsList(stats);
	}
}
