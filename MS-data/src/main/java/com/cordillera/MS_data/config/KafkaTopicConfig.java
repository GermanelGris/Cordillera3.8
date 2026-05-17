package com.cordillera.MS_data.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic topicDatosIngresados() {
        return TopicBuilder.name("datos-ingresados")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
