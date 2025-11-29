package com.grouptaskmanager.report.kafka;

import com.grouptaskmanager.report.model.DailyTaskEntry;
import com.grouptaskmanager.report.repository.DailyTaskEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyTaskEventConsumer {

    private final DailyTaskEntryRepository dailyTaskEntryRepository;

    @KafkaListener(
            topics = "${kafka.topic.daily-task-archived}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeDailyTaskArchivedEvent(DailyTaskArchivedEvent event) {
        log.info("Received daily task archived event: {}", event);
        
        DailyTaskEntry entry = DailyTaskEntry.builder()
                .dailyTaskId(event.getDailyTaskId())
                .description(event.getDescription())
                .wasDone(event.isWasDone())
                .date(event.getTaskDate())
                .groupId(event.getGroupId())
                .assigneeUserId(event.getAssigneeUserId())
                .build();
        
        dailyTaskEntryRepository.save(entry);
        log.info("Saved daily task entry: {}", entry);
    }
}

