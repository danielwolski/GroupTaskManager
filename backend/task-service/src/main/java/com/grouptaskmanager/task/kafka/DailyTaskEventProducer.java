package com.grouptaskmanager.task.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyTaskEventProducer {

    private final KafkaTemplate<String, DailyTaskArchivedEvent> kafkaTemplate;

    @Value("${kafka.topic.daily-task-archived}")
    private String dailyTaskArchivedTopic;

    public void sendDailyTaskArchivedEvent(DailyTaskArchivedEvent event) {
        log.info("Sending daily task archived event: {}", event);
        kafkaTemplate.send(dailyTaskArchivedTopic, String.valueOf(event.getDailyTaskId()), event);
    }
}

