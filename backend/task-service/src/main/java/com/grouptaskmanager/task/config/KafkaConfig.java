package com.grouptaskmanager.task.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Value("${kafka.topic.daily-task-archived}")
    private String dailyTaskArchivedTopic;

    @Bean
    public NewTopic dailyTaskArchivedTopic() {
        return TopicBuilder.name(dailyTaskArchivedTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}

