package com.cordillera.MS_kpi.kafka;

import com.cordillera.MS_kpi.entity.Kpi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KpiEventPublisherTest {

    @Mock KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks KpiEventPublisher publisher;

    @Test
    void publicarKpi_enviaAKafka() {
        Kpi kpi = Kpi.builder().id(1L).nombre("Ventas").tipoCalculo("SUMA")
                .valor(new BigDecimal("150000")).periodo("2026-05").unidad("CLP").build();

        publisher.publicarKpi(kpi);

        verify(kafkaTemplate).send(eq("kpi-calculado"), eq("2026-05"), contains("Ventas"));
    }

    @Test
    void publicarKpi_unidadNula_usaClpPorDefecto() {
        Kpi kpi = Kpi.builder().id(2L).nombre("X").tipoCalculo("SUMA")
                .valor(new BigDecimal("10")).periodo("2026-06").unidad(null).build();

        publisher.publicarKpi(kpi);

        verify(kafkaTemplate).send(eq("kpi-calculado"), eq("2026-06"), contains("CLP"));
    }
}
