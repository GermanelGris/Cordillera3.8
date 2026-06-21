package com.cordillera.MS_data.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Vista de la configuración SMTP que se devuelve al frontend.
 * Nunca expone la contraseña: solo indica si hay una configurada.
 */
@Data
@Builder
public class MailConfigResponse {

    private String host;
    private Integer port;
    private String username;
    private String fromAddress;
    private Boolean auth;
    private Boolean starttls;
    private boolean passwordConfigurada;
    private LocalDateTime updatedAt;
}
