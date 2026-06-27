package com.cordillera.MS_kpi.kafka;

import com.cordillera.MS_kpi.entity.Kpi;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KpiCorreoPublisherTest {

    @Mock KafkaTemplate<String, String> kafkaTemplate;
    @Spy  ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks KpiCorreoPublisher publisher;

    @Test
    void publicar_enviaSolicitudPorCorreo() {
        Kpi kpi = Kpi.builder().id(1L).nombre("Ventas").tipoCalculo("SUMA")
                .valor(new BigDecimal("150000")).periodo("2026-05").unidad("CLP")
                .descripcion("desc").createdAt(LocalDateTime.now()).build();

        publisher.publicar(kpi, "cliente@correo.cl");

        verify(kafkaTemplate).send(eq("reporte-correo"), eq("cliente@correo.cl"), anyString());
    }

    @Test
    void publicar_conCamposNulos_noLanza() {
        Kpi kpi = Kpi.builder().id(2L).build(); // todos los textos nulos

        publisher.publicar(kpi, "x@y.cl");

        verify(kafkaTemplate).send(eq("reporte-correo"), eq("x@y.cl"), anyString());
    }
}
