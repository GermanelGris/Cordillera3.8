package com.cordillera.MS_kpi.kafka;

import com.cordillera.MS_kpi.entity.Kpi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KpiEventPublisher {

    private static final String TOPIC = "kpi-calculado";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void publicarKpi(Kpi kpi) {
        String mensaje = String.format(
                "{\"id\":%d,\"nombre\":\"%s\",\"tipoCalculo\":\"%s\",\"valor\":%s,\"periodo\":\"%s\",\"unidad\":\"%s\"}",
                kpi.getId(),
                kpi.getNombre(),
                kpi.getTipoCalculo(),
                kpi.getValor().toPlainString(),
                kpi.getPeriodo(),
                kpi.getUnidad() != null ? kpi.getUnidad() : "CLP"
        );
        kafkaTemplate.send(TOPIC, kpi.getPeriodo(), mensaje);
        log.info("KPI publicado en Kafka - topic: {}, nombre: {}, periodo: {}", TOPIC, kpi.getNombre(), kpi.getPeriodo());
    }
}
