package com.cordillera.MS_data.kafka;

import com.cordillera.MS_data.dto.ArchivoReporte;
import com.cordillera.MS_data.dto.ReporteCorreoDto;
import com.cordillera.MS_data.service.DocumentoTabularService;
import com.cordillera.MS_data.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Consume solicitudes de "reporte por correo" publicadas por MS-reportes y MS-kpi.
 * Genera PDF + CSV + XLSX y los envía por correo de forma asíncrona.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReporteCorreoConsumer {

    private final ObjectMapper objectMapper;
    private final DocumentoTabularService documentoTabularService;
    private final EmailService emailService;

    @KafkaListener(topics = "reporte-correo", groupId = "ms-data-group")
    public void consumir(String mensaje) {
        log.info("MS-DATA recibió solicitud de reporte por correo: {}", mensaje);
        try {
            ReporteCorreoDto dto = objectMapper.readValue(mensaje, ReporteCorreoDto.class);

            if (dto.getDestinatario() == null || dto.getDestinatario().isBlank()) {
                log.warn("Solicitud de reporte por correo sin destinatario, se descarta");
                return;
            }

            List<ArchivoReporte> adjuntos = documentoTabularService.generarTodos(
                    dto.getTitulo(), dto.getColumnas(), dto.getFilas());

            String asunto = dto.getAsunto() != null && !dto.getAsunto().isBlank()
                    ? dto.getAsunto()
                    : (dto.getTitulo() != null ? dto.getTitulo() : "Reporte");

            emailService.enviarConAdjuntos(dto.getDestinatario(), asunto, dto.getCuerpo(), adjuntos);

            log.info("Reporte enviado por correo a {} ({} adjuntos)", dto.getDestinatario(), adjuntos.size());
        } catch (Exception e) {
            log.error("Error procesando solicitud de reporte por correo: {}", e.getMessage(), e);
        }
    }
}
