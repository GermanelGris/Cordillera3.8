package com.cordillera.MS_reporte_mail.kafka;

import com.cordillera.MS_reporte_mail.config.KafkaTopicConfig;
import com.cordillera.MS_reporte_mail.dto.SolicitudReporteDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SolicitudReportePublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publicarSolicitud(SolicitudReporteDto solicitud) {
        try {
            // Normaliza el formato a mayúsculas para que MS-DATA lo interprete sin ambigüedad
            solicitud.setFormato(solicitud.getFormato().toUpperCase());
            String mensaje = objectMapper.writeValueAsString(solicitud);
            kafkaTemplate.send(KafkaTopicConfig.TOPIC_SOLICITUD_REPORTE, solicitud.getDestinatario(), mensaje);
            log.info("Solicitud de reporte publicada - topic: {}, destinatario: {}, formato: {}",
                    KafkaTopicConfig.TOPIC_SOLICITUD_REPORTE, solicitud.getDestinatario(), solicitud.getFormato());
        } catch (JsonProcessingException e) {
            log.error("Error serializando la solicitud de reporte: {}", e.getMessage());
            throw new IllegalStateException("No se pudo serializar la solicitud de reporte", e);
        }
    }
}
