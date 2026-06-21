package com.cordillera.MS_data.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Datos de configuración SMTP enviados por el administrador.
 */
@Data
public class MailConfigDto {

    @NotBlank(message = "El host SMTP es obligatorio")
    private String host;

    @NotNull(message = "El puerto es obligatorio")
    @Min(value = 1, message = "Puerto inválido")
    @Max(value = 65535, message = "Puerto inválido")
    private Integer port;

    private String username;

    /** Si llega vacío en una actualización, se conserva la contraseña existente. */
    private String password;

    private String fromAddress;

    private Boolean auth;

    private Boolean starttls;
}
