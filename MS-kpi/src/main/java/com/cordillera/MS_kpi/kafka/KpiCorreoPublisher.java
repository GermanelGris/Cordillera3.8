package com.cordillera.MS_kpi.kafka;

import com.cordillera.MS_kpi.entity.Kpi;
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
 * Publica en Kafka la solicitud para que MS-data genere el KPI en
 * PDF + CSV + XLSX y lo envíe por correo (procesamiento asíncrono).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KpiCorreoPublisher {

    private static final String TOPIC = "reporte-correo";
    private static final DateTimeFormatter FECHA_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publicar(Kpi kpi, String destinatario) {
        try {
            List<List<String>> filas = new ArrayList<>();
            filas.add(List.of("Nombre", nz(kpi.getNombre())));
            filas.add(List.of("Tipo de cálculo", nz(kpi.getTipoCalculo())));
            filas.add(List.of("Valor", kpi.getValor() != null ? kpi.getValor().toPlainString() : ""));
            filas.add(List.of("Periodo", nz(kpi.getPeriodo())));
            filas.add(List.of("Unidad", nz(kpi.getUnidad())));
            filas.add(List.of("Descripción", nz(kpi.getDescripcion())));
            filas.add(List.of("Fecha", kpi.getCreatedAt() != null ? kpi.getCreatedAt().format(FECHA_FMT) : ""));

            Map<String, Object> mensaje = new LinkedHashMap<>();
            mensaje.put("destinatario", destinatario);
            mensaje.put("asunto", "KPI: " + nz(kpi.getNombre()));
            mensaje.put("cuerpo", "Adjunto el KPI \"" + nz(kpi.getNombre())
                    + "\" del periodo " + nz(kpi.getPeriodo()) + " en PDF, CSV y XLSX.");
            mensaje.put("titulo", "KPI - " + nz(kpi.getNombre()));
            mensaje.put("columnas", List.of("Campo", "Valor"));
            mensaje.put("filas", filas);

            kafkaTemplate.send(TOPIC, destinatario, objectMapper.writeValueAsString(mensaje));
            log.info("Solicitud de KPI por correo publicada - topic: {}, destinatario: {}", TOPIC, destinatario);
        } catch (Exception e) {
            log.error("Error publicando KPI por correo: {}", e.getMessage(), e);
        }
    }

    private String nz(String v) {
        return v == null ? "" : v;
    }
}
