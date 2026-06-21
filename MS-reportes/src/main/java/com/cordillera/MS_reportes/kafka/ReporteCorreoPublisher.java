package com.cordillera.MS_reportes.kafka;

import com.cordillera.MS_reportes.entity.Reporte;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Publica en Kafka la solicitud para que MS-data genere el reporte en
 * PDF + CSV + XLSX y lo envíe por correo (procesamiento asíncrono).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReporteCorreoPublisher {

    private static final String TOPIC = "reporte-correo";
    private static final DateTimeFormatter FECHA_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publicar(Reporte reporte, String destinatario) {
        try {
            List<List<String>> filas = new ArrayList<>();
            filas.add(List.of("Tipo", nz(reporte.getTipo())));
            filas.add(List.of("Título", nz(reporte.getTitulo())));
            filas.add(List.of("Periodo", nz(reporte.getPeriodo())));
            filas.add(List.of("Estado", nz(reporte.getEstado())));
            filas.add(List.of("Generado por", nz(reporte.getGeneradoPor())));
            filas.add(List.of("Fecha", reporte.getCreatedAt() != null ? reporte.getCreatedAt().format(FECHA_FMT) : ""));
            filas.add(List.of("Contenido", nz(reporte.getContenido())));

            Map<String, Object> mensaje = new LinkedHashMap<>();
            mensaje.put("destinatario", destinatario);
            mensaje.put("asunto", "Reporte: " + nz(reporte.getTitulo()));
            mensaje.put("cuerpo", "Adjunto el reporte \"" + nz(reporte.getTitulo())
                    + "\" del periodo " + nz(reporte.getPeriodo()) + " en PDF, CSV y XLSX.");
            mensaje.put("titulo", nz(reporte.getTitulo()));
            mensaje.put("columnas", List.of("Campo", "Valor"));
            mensaje.put("filas", filas);

            kafkaTemplate.send(TOPIC, destinatario, objectMapper.writeValueAsString(mensaje));
            log.info("Solicitud de reporte por correo publicada - topic: {}, destinatario: {}", TOPIC, destinatario);
        } catch (Exception e) {
            log.error("Error publicando reporte por correo: {}", e.getMessage(), e);
        }
    }

    private String nz(String v) {
        return v == null ? "" : v;
    }
}
