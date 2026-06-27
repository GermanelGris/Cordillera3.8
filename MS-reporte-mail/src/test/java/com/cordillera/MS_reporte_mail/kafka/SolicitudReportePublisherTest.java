package com.cordillera.MS_reporte_mail.kafka;

import com.cordillera.MS_reporte_mail.config.KafkaTopicConfig;
import com.cordillera.MS_reporte_mail.dto.SolicitudReporteDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitudReportePublisherTest {

    @Mock KafkaTemplate<String, String> kafkaTemplate;
    @Mock ObjectMapper objectMapper;

    @InjectMocks SolicitudReportePublisher publisher;

    private SolicitudReporteDto dto() {
        SolicitudReporteDto d = new SolicitudReporteDto();
        d.setDestinatario("cliente@correo.cl");
        d.setAsunto("Reporte");
        d.setFormato("pdf"); // minúscula a propósito → debe normalizarse
        d.setPeriodo("2026-05");
        return d;
    }

    @Test
    void publicarSolicitud_normalizaFormatoYEnviaAKafka() throws Exception {
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"json\":true}");
        SolicitudReporteDto dto = dto();

        publisher.publicarSolicitud(dto);

        assertThat(dto.getFormato()).isEqualTo("PDF"); // normalizado a mayúsculas
        verify(kafkaTemplate).send(eq(KafkaTopicConfig.TOPIC_SOLICITUD_REPORTE),
                eq("cliente@correo.cl"), eq("{\"json\":true}"));
    }

    @Test
    void publicarSolicitud_errorSerializacion_lanzaIllegalState() throws Exception {
        when(objectMapper.writeValueAsString(any())).thenThrow(JsonProcessingException.class);

        assertThatThrownBy(() -> publisher.publicarSolicitud(dto()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No se pudo serializar");
    }
}
