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
            topics = "${kafka.topic.daily-tasks}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeDailyTaskEvent(DailyTaskEvent event) {
        log.info("Received daily task event: {} - {}", event.getEventType(), event.getDailyTaskId());
        
        switch (event.getEventType()) {
            case CREATED:
                handleCreatedEvent(event);
                break;
            case UPDATED:
                handleUpdatedEvent(event);
                break;
            case DELETED:
                handleDeletedEvent(event);
                break;
            case DAY_RESET:
                handleDayResetEvent(event);
                break;
            default:
                log.warn("Unknown event type: {}", event.getEventType());
        }
    }

    private void handleCreatedEvent(DailyTaskEvent event) {
        DailyTaskEntry entry = DailyTaskEntry.builder()
                .dailyTaskId(event.getDailyTaskId())
                .description(event.getDescription())
                .wasDone(event.isDone())
                .date(event.getTaskDate())
                .groupId(event.getGroupId())
                .assigneeUserId(event.getAssigneeUserId())
                .build();
        
        dailyTaskEntryRepository.save(entry);
        log.info("Saved new daily task entry: {}", entry.getDailyTaskId());
    }

    private void handleUpdatedEvent(DailyTaskEvent event) {
        // Update existing entry for today or create new one
        dailyTaskEntryRepository.findByDailyTaskIdAndDate(event.getDailyTaskId(), event.getTaskDate())
                .ifPresentOrElse(
                        entry -> {
                            entry.setWasDone(event.isDone());
                            entry.setDescription(event.getDescription());
                            entry.setAssigneeUserId(event.getAssigneeUserId());
                            dailyTaskEntryRepository.save(entry);
                            log.info("Updated daily task entry: {}", entry.getDailyTaskId());
                        },
                        () -> {
                            // Create new entry if not found
                            handleCreatedEvent(event);
                        }
                );
    }

    private void handleDeletedEvent(DailyTaskEvent event) {
        // Mark entry as deleted or just log it
        log.info("Daily task deleted: {}", event.getDailyTaskId());
        // Optionally: keep history or delete entries
    }

    private void handleDayResetEvent(DailyTaskEvent event) {
        // This captures the final state of a task at the end of the day
        DailyTaskEntry entry = DailyTaskEntry.builder()
                .dailyTaskId(event.getDailyTaskId())
                .description(event.getDescription())
                .wasDone(event.isDone())
                .date(event.getTaskDate())
                .groupId(event.getGroupId())
                .assigneeUserId(event.getAssigneeUserId())
                .build();
        
        // Save or update the entry for that date
        dailyTaskEntryRepository.findByDailyTaskIdAndDate(event.getDailyTaskId(), event.getTaskDate())
                .ifPresentOrElse(
                        existingEntry -> {
                            existingEntry.setWasDone(event.isDone());
                            dailyTaskEntryRepository.save(existingEntry);
                            log.info("Updated day reset entry: {}", existingEntry.getDailyTaskId());
                        },
                        () -> {
                            dailyTaskEntryRepository.save(entry);
                            log.info("Saved day reset entry: {}", entry.getDailyTaskId());
                        }
                );
    }
}
