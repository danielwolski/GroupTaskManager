package com.grouptaskmanager.mapper;

import com.grouptaskmanager.model.DailyTaskStats;
import com.grouptaskmanager.rest.dailytask.RestDailyTaskStats;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DailyTaskStatsMapper {
    
    RestDailyTaskStats dailyTaskStatsToRestDailyTaskStats(DailyTaskStats dailyTaskStats);
    
    List<RestDailyTaskStats> dailyTaskStatsListToRestDailyTaskStatsList(List<DailyTaskStats> dailyTaskStatsList);
}
