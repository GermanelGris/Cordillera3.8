package com.cordillera.MS_reportes.kafka;

import com.cordillera.MS_reportes.dto.ReporteResponse;
import com.cordillera.MS_reportes.service.ReporteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReporteEventConsumerTest {

    @Mock ReporteService reporteService;

    @InjectMocks ReporteEventConsumer consumer;

    @Test
    void consumir_kpiValido_generaReporteAutomatico() {
        when(reporteService.generar(any())).thenReturn(ReporteResponse.builder().id(1L).build());
        String json = "{\"id\":1,\"nombre\":\"Ventas\",\"periodo\":\"2026-05\"}";

        consumer.consumirKpiCalculado(json);

        verify(reporteService).generar(any());
    }

    @Test
    void consumir_sinPeriodo_noGenera() {
        String json = "{\"id\":1,\"nombre\":\"Ventas\"}";

        consumer.consumirKpiCalculado(json);

        verify(reporteService, never()).generar(any());
    }

    @Test
    void consumir_mensajeInvalido_noLanzaExcepcion() {
        consumer.consumirKpiCalculado("no es json");

        verify(reporteService, never()).generar(any());
    }
}
