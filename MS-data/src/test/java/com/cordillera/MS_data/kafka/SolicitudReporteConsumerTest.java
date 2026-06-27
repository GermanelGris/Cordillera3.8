package com.cordillera.MS_data.kafka;

import com.cordillera.MS_data.dto.ArchivoReporte;
import com.cordillera.MS_data.dto.EstadisticasStock;
import com.cordillera.MS_data.service.EmailService;
import com.cordillera.MS_data.service.EstadisticaStockService;
import com.cordillera.MS_data.service.ReporteGeneradorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitudReporteConsumerTest {

    @Spy  ObjectMapper objectMapper = new ObjectMapper();
    @Mock EstadisticaStockService estadisticaStockService;
    @Mock ReporteGeneradorService reporteGeneradorService;
    @Mock EmailService emailService;

    @InjectMocks SolicitudReporteConsumer consumer;

    @Test
    void consumir_solicitudValida_generaYEnviaReporte() throws Exception {
        String msg = "{\"destinatario\":\"a@b.cl\",\"asunto\":\"A\",\"cuerpo\":\"C\",\"formato\":\"PDF\",\"periodo\":\"2026-05\"}";
        when(estadisticaStockService.calcular()).thenReturn(EstadisticasStock.builder().build());
        when(estadisticaStockService.listarInventario()).thenReturn(List.of());
        when(reporteGeneradorService.generar(eq("PDF"), eq("2026-05"), any(), anyList()))
                .thenReturn(new ArchivoReporte("reporte-stock.pdf", "application/pdf", new byte[]{1}));

        consumer.consumir(msg);

        verify(emailService).enviarConAdjunto(eq("a@b.cl"), eq("A"), eq("C"), any(ArchivoReporte.class));
    }

    @Test
    void consumir_sinDestinatario_seDescarta() throws Exception {
        String msg = "{\"destinatario\":\"\",\"formato\":\"PDF\"}";

        consumer.consumir(msg);

        verify(emailService, never()).enviarConAdjunto(any(), any(), any(), any());
    }

    @Test
    void consumir_mensajeInvalido_noLanzaExcepcion() throws Exception {
        consumer.consumir("no es json");

        verify(emailService, never()).enviarConAdjunto(any(), any(), any(), any());
    }
}
