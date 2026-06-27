package com.cordillera.MS_reporte_mail.controller;

import com.cordillera.MS_reporte_mail.dto.SolicitudReporteDto;
import com.cordillera.MS_reporte_mail.kafka.SolicitudReportePublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class ReporteMailControllerTest {

    @Mock SolicitudReportePublisher publisher;

    @InjectMocks ReporteMailController controller;

    @Test
    void solicitar_devuelve202YPublicaEnKafka() {
        SolicitudReporteDto dto = new SolicitudReporteDto();
        dto.setDestinatario("cliente@correo.cl");
        dto.setAsunto("Reporte de Stock");
        dto.setFormato("PDF");

        var r = controller.solicitar(dto);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(r.getBody().get("estado")).isEqualTo("ACEPTADO");
        assertThat(r.getBody().get("mensaje")).contains("cliente@correo.cl");
        verify(publisher).publicarSolicitud(dto);
    }

    @Test
    void health_ok() {
        assertThat(controller.health().getBody()).contains("OK");
    }
}
