package com.cordillera.MS_login.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthEventPublisher {

    private static final String TOPIC = "usuario-autenticado";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void publicarLogin(String username) {
        String mensaje = String.format(
                "{\"username\":\"%s\",\"timestamp\":\"%s\",\"evento\":\"LOGIN\"}",
                username, LocalDateTime.now()
        );
        kafkaTemplate.send(TOPIC, username, mensaje);
        log.info("Evento login publicado en Kafka para usuario: {}", username);
    }
}
