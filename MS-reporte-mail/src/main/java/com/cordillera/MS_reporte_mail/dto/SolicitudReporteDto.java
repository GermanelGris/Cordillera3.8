package com.cordillera.MS_reporte_mail.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SolicitudReporteDto {

    @NotBlank(message = "El destinatario es obligatorio")
    @Email(message = "El destinatario debe ser un correo válido")
    private String destinatario;

    @NotBlank(message = "El asunto es obligatorio")
    private String asunto;

    private String cuerpo;

    @NotBlank(message = "El formato es obligatorio (PDF o EXCEL)")
    @Pattern(regexp = "(?i)PDF|EXCEL", message = "El formato debe ser PDF o EXCEL")
    private String formato;

    /** Etiqueta del periodo a mostrar en el reporte (opcional, formato YYYY-MM). */
    private String periodo;
}
