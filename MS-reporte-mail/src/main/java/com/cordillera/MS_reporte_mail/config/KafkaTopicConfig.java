package com.cordillera.MS_reporte_mail.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String TOPIC_SOLICITUD_REPORTE = "solicitud-reporte";

    @Bean
    public NewTopic topicSolicitudReporte() {
        return TopicBuilder.name(TOPIC_SOLICITUD_REPORTE)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
