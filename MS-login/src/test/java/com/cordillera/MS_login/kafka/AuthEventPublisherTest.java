package com.cordillera.MS_login.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthEventPublisherTest {

    @Mock KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks AuthEventPublisher publisher;

    @Test
    void publicarLogin_enviaMensajeAKafka() {
        publisher.publicarLogin("admin@cordillera.cl");

        verify(kafkaTemplate).send(eq("usuario-autenticado"), eq("admin@cordillera.cl"), contains("LOGIN"));
    }
}
