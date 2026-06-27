package com.cordillera.MS_data.kafka;

import com.cordillera.MS_data.entity.DatoIngresado;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatoEventPublisherTest {

    @Mock KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks DatoEventPublisher publisher;

    @Test
    void publicarDato_enviaAKafka() {
        DatoIngresado dato = DatoIngresado.builder()
                .id(1L).fuente("ERP").tipo("VENTA").valor(new BigDecimal("75000"))
                .periodo("2026-05").descripcion("Venta").procesado(false).build();

        publisher.publicarDato(dato);

        verify(kafkaTemplate).send(eq("datos-ingresados"), eq("2026-05"), contains("VENTA"));
    }

    @Test
    void publicarDato_descripcionNula_noFalla() {
        DatoIngresado dato = DatoIngresado.builder()
                .id(2L).fuente("ERP").tipo("GASTO").valor(new BigDecimal("100"))
                .periodo("2026-06").descripcion(null).procesado(false).build();

        publisher.publicarDato(dato);

        verify(kafkaTemplate).send(eq("datos-ingresados"), eq("2026-06"), anyString());
    }
}
