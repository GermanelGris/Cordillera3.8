package com.cordillera.MS_data.kafka;

import com.cordillera.MS_data.dto.ArchivoReporte;
import com.cordillera.MS_data.service.DocumentoTabularService;
import com.cordillera.MS_data.service.EmailService;
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
class ReporteCorreoConsumerTest {

    @Spy  ObjectMapper objectMapper = new ObjectMapper();
    @Mock DocumentoTabularService documentoTabularService;
    @Mock EmailService emailService;

    @InjectMocks ReporteCorreoConsumer consumer;

    @Test
    void consumir_solicitudValida_generaYEnviaAdjuntos() throws Exception {
        String msg = "{\"destinatario\":\"a@b.cl\",\"asunto\":\"Asunto\",\"cuerpo\":\"Cuerpo\","
                + "\"titulo\":\"T\",\"columnas\":[\"A\",\"B\"],\"filas\":[[\"1\",\"2\"]]}";
        when(documentoTabularService.generarTodos(any(), any(), any()))
                .thenReturn(List.of(new ArchivoReporte("r.pdf", "application/pdf", new byte[]{1})));

        consumer.consumir(msg);

        verify(emailService).enviarConAdjuntos(eq("a@b.cl"), eq("Asunto"), eq("Cuerpo"), anyList());
    }

    @Test
    void consumir_sinAsunto_usaTituloComoAsunto() throws Exception {
        String msg = "{\"destinatario\":\"a@b.cl\",\"titulo\":\"Mi Titulo\",\"columnas\":[\"A\"],\"filas\":[[\"1\"]]}";
        when(documentoTabularService.generarTodos(any(), any(), any()))
                .thenReturn(List.of(new ArchivoReporte("r.pdf", "application/pdf", new byte[]{1})));

        consumer.consumir(msg);

        verify(emailService).enviarConAdjuntos(eq("a@b.cl"), eq("Mi Titulo"), any(), anyList());
    }

    @Test
    void consumir_sinDestinatario_seDescarta() throws Exception {
        consumer.consumir("{\"titulo\":\"T\"}");

        verify(emailService, never()).enviarConAdjuntos(any(), any(), any(), anyList());
    }

    @Test
    void consumir_mensajeInvalido_noLanzaExcepcion() throws Exception {
        consumer.consumir("no es json");

        verify(emailService, never()).enviarConAdjuntos(any(), any(), any(), anyList());
    }
}
