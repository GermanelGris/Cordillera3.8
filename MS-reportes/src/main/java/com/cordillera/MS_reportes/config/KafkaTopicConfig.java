package com.cordillera.MS_reportes.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic topicReporteGenerado() {
        return TopicBuilder.name("reporte-generado")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
