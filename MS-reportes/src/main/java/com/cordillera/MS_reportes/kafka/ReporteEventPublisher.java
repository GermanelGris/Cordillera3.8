package com.cordillera.MS_reportes.kafka;

import com.cordillera.MS_reportes.entity.Reporte;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReporteEventPublisher {

    private static final String TOPIC = "reporte-generado";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void publicarReporte(Reporte reporte) {
        String mensaje = String.format(
                "{\"id\":%d,\"tipo\":\"%s\",\"titulo\":\"%s\",\"periodo\":\"%s\",\"estado\":\"%s\"}",
                reporte.getId(),
                reporte.getTipo(),
                reporte.getTitulo(),
                reporte.getPeriodo(),
                reporte.getEstado()
        );
        kafkaTemplate.send(TOPIC, reporte.getPeriodo(), mensaje);
        log.info("Reporte publicado en Kafka - topic: {}, id: {}, tipo: {}", TOPIC, reporte.getId(), reporte.getTipo());
    }
}
