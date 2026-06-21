package com.cordillera.MS_reportes.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReporteDto {

    @NotBlank(message = "El tipo es obligatorio (KPI, MENSUAL, RESUMEN)")
    private String tipo;

    @NotBlank(message = "El título es obligatorio")
    private String titulo;

    @NotBlank(message = "El periodo es obligatorio")
    private String periodo;

    private String generadoPor;
    private String descripcionAdicional;

    /** Si se indica, el reporte generado se envía a este correo en PDF + CSV + XLSX. */
    private String destinatario;
}
