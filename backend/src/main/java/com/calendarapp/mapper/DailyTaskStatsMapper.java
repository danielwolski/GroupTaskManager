package com.calendarapp.mapper;

import com.calendarapp.model.DailyTaskStats;
import com.calendarapp.rest.dailytask.RestDailyTaskStats;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DailyTaskStatsMapper {
    
    RestDailyTaskStats dailyTaskStatsToRestDailyTaskStats(DailyTaskStats dailyTaskStats);
    
    List<RestDailyTaskStats> dailyTaskStatsListToRestDailyTaskStatsList(List<DailyTaskStats> dailyTaskStatsList);
}
