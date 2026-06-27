package com.cordillera.MS_kpi.kafka;

import com.cordillera.MS_kpi.service.KpiService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KpiEventConsumerTest {

    @Mock KpiService kpiService;

    @InjectMocks KpiEventConsumer consumer;

    @Test
    void consumir_mensajeValido_calculaKpiAutomatico() {
        String json = "{\"id\":1,\"tipo\":\"VENTA\",\"valor\":75000,\"periodo\":\"2026-05\"}";

        consumer.consumirDatoIngresado(json);

        verify(kpiService).calcular(any());
    }

    @Test
    void consumir_mensajeSinValor_noCalcula() {
        String json = "{\"tipo\":\"VENTA\",\"periodo\":\"2026-05\"}";

        consumer.consumirDatoIngresado(json);

        verify(kpiService, never()).calcular(any());
    }

    @Test
    void consumir_mensajeInvalido_noLanzaExcepcion() {
        consumer.consumirDatoIngresado("esto no es json");

        verify(kpiService, never()).calcular(any());
    }
}
