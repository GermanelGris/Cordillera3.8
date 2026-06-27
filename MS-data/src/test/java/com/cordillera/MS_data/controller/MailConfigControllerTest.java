package com.cordillera.MS_data.controller;

import com.cordillera.MS_data.dto.MailConfigDto;
import com.cordillera.MS_data.dto.MailConfigResponse;
import com.cordillera.MS_data.service.EmailService;
import com.cordillera.MS_data.service.MailConfigService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class MailConfigControllerTest {

    @Mock MailConfigService mailConfigService;
    @Mock EmailService emailService;

    @InjectMocks MailConfigController controller;

    @Test
    void obtener_devuelve200() {
        when(mailConfigService.obtenerResponse()).thenReturn(MailConfigResponse.builder().host("smtp").build());
        assertThat(controller.obtener().getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void actualizar_devuelve200() {
        when(mailConfigService.guardar(any())).thenReturn(MailConfigResponse.builder().host("smtp").build());
        assertThat(controller.actualizar(new MailConfigDto()).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void probar_envioOk_retornaEstadoOk() throws Exception {
        doNothing().when(emailService).enviarPrueba("a@b.cl");

        var r = controller.probar("a@b.cl");

        assertThat(r.getBody().get("estado")).isEqualTo("OK");
    }

    @Test
    void probar_conDireccionDuplicada_usaPrimera() throws Exception {
        doNothing().when(emailService).enviarPrueba("a@b.cl");

        var r = controller.probar("a@b.cl,a@b.cl");

        assertThat(r.getBody().get("mensaje")).contains("a@b.cl");
    }

    @Test
    void probar_envioFalla_retornaEstadoError() throws Exception {
        doThrow(new RuntimeException("SMTP caído")).when(emailService).enviarPrueba("a@b.cl");

        var r = controller.probar("a@b.cl");

        assertThat(r.getBody().get("estado")).isEqualTo("ERROR");
        assertThat(r.getBody().get("mensaje")).contains("SMTP caído");
    }
}
