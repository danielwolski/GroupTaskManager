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

    private final KafkaTemplate<String, DailyTaskEvent> kafkaTemplate;

    @Value("${kafka.topic.daily-tasks}")
    private String dailyTasksTopic;

    public void sendDailyTaskEvent(DailyTaskEvent event) {
        log.info("Sending daily task event: {} - {}", event.getEventType(), event.getDailyTaskId());
        kafkaTemplate.send(dailyTasksTopic, String.valueOf(event.getDailyTaskId()), event);
    }
}
