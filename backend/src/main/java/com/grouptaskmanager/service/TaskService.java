package com.grouptaskmanager.service;

import java.util.List;

import com.grouptaskmanager.model.Group;
import org.springframework.stereotype.Service;

import com.grouptaskmanager.mapper.TaskMapper;
import com.grouptaskmanager.model.Task;
import com.grouptaskmanager.repository.TaskRepository;
import com.grouptaskmanager.rest.task.RestCreateTask;

import com.grouptaskmanager.rest.task.RestTask;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class TaskService {
	private final TaskMapper taskMapper;
	private final TaskRepository taskRepository;
	private final UserService userService;

	public Task createTask(RestCreateTask restTask) {
		Task task = taskMapper.restCreateTaskToTask(restTask);
		task.setGroup(userService.getCurrentUserGroup());
		if (restTask.getAssigneeUserId() != null) {
			task.setAssignee(userService.getUserById(restTask.getAssigneeUserId()));
		}
		Task savedTask = taskRepository.save(task);
		log.info("Task saved: {}", savedTask);
		return savedTask;
	}

	public List<RestTask> getAllTasksForGroup() {
		Group currentUserGroup = userService.getCurrentUserGroup();
		return taskMapper.taskListToTaskRestList(taskRepository.findAllByGroup(currentUserGroup));
	}

	public void deleteTask(Long id) {
		taskRepository.deleteById(id);
	}

	public void toggleIsDone(Long id) {
		taskRepository.toggleIsDone(id);
	}
}
