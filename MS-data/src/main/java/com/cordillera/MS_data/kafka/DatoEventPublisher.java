package com.cordillera.MS_data.kafka;

import com.cordillera.MS_data.entity.DatoIngresado;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatoEventPublisher {

    private static final String TOPIC = "datos-ingresados";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void publicarDato(DatoIngresado dato) {
        String mensaje = String.format(
                "{\"id\":%d,\"fuente\":\"%s\",\"tipo\":\"%s\",\"valor\":%s,\"periodo\":\"%s\",\"descripcion\":\"%s\"}",
                dato.getId(),
                dato.getFuente(),
                dato.getTipo(),
                dato.getValor().toPlainString(),
                dato.getPeriodo(),
                dato.getDescripcion() != null ? dato.getDescripcion() : ""
        );
        kafkaTemplate.send(TOPIC, dato.getPeriodo(), mensaje);
        log.info("Dato publicado en Kafka - topic: {}, id: {}, tipo: {}", TOPIC, dato.getId(), dato.getTipo());
    }
}
