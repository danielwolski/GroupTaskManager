package com.calendarapp.service;

import java.util.List;

import com.calendarapp.mapper.DailyTaskMapper;
import com.calendarapp.model.Group;
import com.calendarapp.repository.DailyTaskRepository;
import com.calendarapp.rest.dailytask.RestDailyTask;
import org.springframework.stereotype.Service;

import com.calendarapp.model.DailyTask;
import com.calendarapp.rest.dailytask.RestCreateDailyTask;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class DailyTaskService {
	private final DailyTaskMapper dailyTaskMapper;
	private final DailyTaskRepository dailyTaskRepository;
	private final UserService userService;

	public DailyTask createDailyTask(RestCreateDailyTask restDailyTask) {
		DailyTask dailyTask = dailyTaskMapper.restCreateDailyTaskToDailyTask(restDailyTask);
		dailyTask.setGroup(userService.getCurrentUserGroup());
		dailyTask.setCreatedBy(userService.getCurrentUser());
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
}
