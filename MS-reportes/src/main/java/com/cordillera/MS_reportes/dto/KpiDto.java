package com.cordillera.MS_reportes.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class KpiDto {
    private Long id;
    private String nombre;
    private String tipoCalculo;
    private BigDecimal valor;
    private String periodo;
    private String unidad;
    private String descripcion;
    private LocalDateTime createdAt;
}
