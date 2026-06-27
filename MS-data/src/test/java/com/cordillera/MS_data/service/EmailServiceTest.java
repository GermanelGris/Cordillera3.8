package com.cordillera.MS_data.service;

import com.cordillera.MS_data.dto.ArchivoReporte;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock MailConfigService mailConfigService;
    @Mock JavaMailSenderImpl mailSender;

    @InjectMocks EmailService emailService;

    @BeforeEach
    void setUp() {
        when(mailConfigService.crearMailSender()).thenReturn(mailSender);
        when(mailConfigService.obtenerRemitente()).thenReturn("no-reply@cordillera.cl");
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((jakarta.mail.Session) null));
    }

    private ArchivoReporte adjunto() {
        return new ArchivoReporte("reporte.pdf", "application/pdf", new byte[]{1, 2, 3});
    }

    @Test
    void enviarConAdjunto_enviaCorreo() throws Exception {
        emailService.enviarConAdjunto("cliente@correo.cl", "Asunto", "Cuerpo", adjunto());

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void enviarConAdjuntos_asuntoYCuerpoVacios_usaValoresPorDefecto() throws Exception {
        emailService.enviarConAdjuntos("cliente@correo.cl", "", "", List.of(adjunto()));

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void enviarPrueba_enviaCorreo() throws Exception {
        emailService.enviarPrueba("cliente@correo.cl");

        verify(mailSender).send(any(MimeMessage.class));
    }
}
