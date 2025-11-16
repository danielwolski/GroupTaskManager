package com.calendarapp.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.calendarapp.model.Task;
import com.calendarapp.rest.task.RestCreateTask;
import com.calendarapp.rest.task.RestTask;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TaskMapper {
	
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "assignee", ignore = true)
	@Mapping(target = "group", ignore = true)
	@Mapping(target = "done", ignore = true)
	Task restCreateTaskToTask(RestCreateTask restTask);

	@Mapping(target = "assignee", expression = "java(task.getAssignee() != null ? task.getAssignee().getUsername() : null)")
	RestTask taskToRestTask(Task task);
	List<RestTask> taskListToTaskRestList(List<Task> taskList);
}
