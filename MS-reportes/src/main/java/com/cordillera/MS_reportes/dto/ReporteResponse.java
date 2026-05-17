package com.cordillera.MS_reportes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteResponse {
    private Long id;
    private String tipo;
    private String titulo;
    private String contenido;
    private String periodo;
    private String estado;
    private String generadoPor;
    private LocalDateTime createdAt;
}
