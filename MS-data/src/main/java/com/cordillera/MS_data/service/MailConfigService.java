package com.cordillera.MS_data.service;

import com.cordillera.MS_data.dto.MailConfigDto;
import com.cordillera.MS_data.dto.MailConfigResponse;
import com.cordillera.MS_data.entity.MailConfig;
import com.cordillera.MS_data.repository.MailConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Properties;

/**
 * Gestiona la configuración SMTP editable por el administrador.
 * Si no hay configuración guardada en BD, usa los valores por defecto de application.properties.
 */
@Service
@RequiredArgsConstructor
public class MailConfigService {

    /** Fila única donde se persiste la configuración. */
    private static final Long SINGLETON_ID = 1L;

    private final MailConfigRepository repository;

    @Value("${spring.mail.host:smtp.gmail.com}")
    private String defHost;
    @Value("${spring.mail.port:587}")
    private Integer defPort;
    @Value("${spring.mail.username:}")
    private String defUsername;
    @Value("${spring.mail.password:}")
    private String defPassword;
    @Value("${app.mail.from:}")
    private String defFrom;

    /** Configuración efectiva: la guardada en BD o, en su defecto, la de properties. */
    public MailConfig obtenerEfectiva() {
        return repository.findById(SINGLETON_ID).orElseGet(this::porDefecto);
    }

    private MailConfig porDefecto() {
        String from = (defFrom == null || defFrom.isBlank()) ? defUsername : defFrom;
        return MailConfig.builder()
                .id(SINGLETON_ID)
                .host(defHost)
                .port(defPort)
                .username(defUsername)
                .password(defPassword)
                .fromAddress(from)
                .auth(Boolean.TRUE)
                .starttls(Boolean.TRUE)
                .build();
    }

    @Transactional
    public MailConfigResponse guardar(MailConfigDto dto) {
        MailConfig cfg = repository.findById(SINGLETON_ID).orElseGet(this::porDefecto);
        cfg.setId(SINGLETON_ID);
        cfg.setHost(dto.getHost());
        cfg.setPort(dto.getPort());
        cfg.setUsername(dto.getUsername());
        // Solo se reemplaza la contraseña si el admin envía una nueva
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            cfg.setPassword(dto.getPassword());
        }
        cfg.setFromAddress((dto.getFromAddress() == null || dto.getFromAddress().isBlank())
                ? dto.getUsername() : dto.getFromAddress());
        cfg.setAuth(dto.getAuth() == null ? Boolean.TRUE : dto.getAuth());
        cfg.setStarttls(dto.getStarttls() == null ? Boolean.TRUE : dto.getStarttls());
        return toResponse(repository.save(cfg));
    }

    public MailConfigResponse obtenerResponse() {
        return toResponse(obtenerEfectiva());
    }

    /** Construye un JavaMailSender a partir de la configuración efectiva. */
    public JavaMailSenderImpl crearMailSender() {
        MailConfig cfg = obtenerEfectiva();
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(cfg.getHost());
        sender.setPort(cfg.getPort());
        sender.setUsername(cfg.getUsername());
        sender.setPassword(cfg.getPassword());
        sender.setDefaultEncoding("UTF-8");

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", String.valueOf(Boolean.TRUE.equals(cfg.getAuth())));
        props.put("mail.smtp.starttls.enable", String.valueOf(Boolean.TRUE.equals(cfg.getStarttls())));
        // Timeouts para que el envío falle rápido en vez de colgarse (JavaMail no los pone por defecto)
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");
        return sender;
    }

    public String obtenerRemitente() {
        MailConfig cfg = obtenerEfectiva();
        return (cfg.getFromAddress() == null || cfg.getFromAddress().isBlank())
                ? cfg.getUsername() : cfg.getFromAddress();
    }

    private MailConfigResponse toResponse(MailConfig cfg) {
        return MailConfigResponse.builder()
                .host(cfg.getHost())
                .port(cfg.getPort())
                .username(cfg.getUsername())
                .fromAddress(cfg.getFromAddress())
                .auth(cfg.getAuth())
                .starttls(cfg.getStarttls())
                .passwordConfigurada(cfg.getPassword() != null && !cfg.getPassword().isBlank())
                .updatedAt(cfg.getUpdatedAt())
                .build();
    }
}
