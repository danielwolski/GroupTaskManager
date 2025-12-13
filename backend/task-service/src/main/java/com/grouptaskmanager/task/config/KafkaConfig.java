package com.grouptaskmanager.task.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Value("${kafka.topic.daily-tasks}")
    private String dailyTasksTopic;

    @Bean
    public NewTopic dailyTasksTopic() {
        return TopicBuilder.name(dailyTasksTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
