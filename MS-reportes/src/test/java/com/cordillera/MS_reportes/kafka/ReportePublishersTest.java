package com.cordillera.MS_reportes.kafka;

import com.cordillera.MS_reportes.entity.Reporte;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportePublishersTest {

    @Mock KafkaTemplate<String, String> kafkaTemplate;
    @Spy  ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks ReporteCorreoPublisher correoPublisher;

    private Reporte reporte() {
        return Reporte.builder().id(1L).tipo("KPI").titulo("Reporte Mayo").contenido("contenido")
                .periodo("2026-05").estado("GENERADO").generadoPor("admin")
                .createdAt(LocalDateTime.now()).build();
    }

    @Test
    void correoPublisher_enviaSolicitud() {
        correoPublisher.publicar(reporte(), "cliente@correo.cl");

        verify(kafkaTemplate).send(eq("reporte-correo"), eq("cliente@correo.cl"), anyString());
    }

    @Test
    void eventPublisher_publicaReporteGenerado() {
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, String> kt = mock(KafkaTemplate.class);
        ReporteEventPublisher eventPublisher = new ReporteEventPublisher(kt);

        eventPublisher.publicarReporte(reporte());

        verify(kt).send(eq("reporte-generado"), eq("2026-05"), contains("KPI"));
    }
}
