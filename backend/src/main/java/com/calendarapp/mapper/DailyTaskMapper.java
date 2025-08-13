package com.calendarapp.mapper;

import java.util.List;

import com.calendarapp.model.DailyTask;
import com.calendarapp.rest.dailytask.RestCreateDailyTask;
import com.calendarapp.rest.dailytask.RestDailyTask;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DailyTaskMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createdBy", ignore = true)
	@Mapping(target = "group", ignore = true)
	@Mapping(target = "done", ignore = true)
	@Mapping(target = "currentDay", ignore = true)
	DailyTask restCreateDailyTaskToDailyTask(RestCreateDailyTask restDailyTask);

	@Mapping(target = "createdBy", source = "createdBy.login")
	RestDailyTask dailyTaskToRestDailyTask(DailyTask dailyTask);
	List<RestDailyTask> dailyTaskListToDailyTaskRestList(List<DailyTask> dailyTaskList);
}
