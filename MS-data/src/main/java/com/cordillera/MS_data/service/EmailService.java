package com.cordillera.MS_data.service;

import com.cordillera.MS_data.dto.ArchivoReporte;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final MailConfigService mailConfigService;

    public void enviarConAdjunto(String destinatario, String asunto, String cuerpo, ArchivoReporte adjunto)
            throws MessagingException {
        enviarConAdjuntos(destinatario, asunto, cuerpo, List.of(adjunto));
    }

    /** Envía un correo con uno o varios adjuntos (PDF, CSV, XLSX, ...). */
    public void enviarConAdjuntos(String destinatario, String asunto, String cuerpo, List<ArchivoReporte> adjuntos)
            throws MessagingException {
        JavaMailSender sender = mailConfigService.crearMailSender();
        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(mailConfigService.obtenerRemitente());
        helper.setTo(destinatario);
        helper.setSubject(asunto != null && !asunto.isBlank() ? asunto : "Reporte");
        helper.setText(cuerpo != null && !cuerpo.isBlank()
                ? cuerpo
                : "Adjunto encontrará el reporte solicitado.");
        for (ArchivoReporte adjunto : adjuntos) {
            helper.addAttachment(adjunto.nombre(), new ByteArrayResource(adjunto.contenido()), adjunto.contentType());
        }

        sender.send(message);
        log.info("Correo enviado a {} con {} adjunto(s)", destinatario, adjuntos.size());
    }

    /** Envía un correo simple para que el administrador verifique la configuración SMTP. */
    public void enviarPrueba(String destinatario) throws MessagingException {
        JavaMailSender sender = mailConfigService.crearMailSender();
        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

        helper.setFrom(mailConfigService.obtenerRemitente());
        helper.setTo(destinatario);
        helper.setSubject("Correo de prueba - Cordillera");
        helper.setText("La configuración de correo se verificó correctamente. "
                + "Este es un mensaje de prueba enviado desde MS-Data.");

        sender.send(message);
        log.info("Correo de prueba enviado a {}", destinatario);
    }
}
