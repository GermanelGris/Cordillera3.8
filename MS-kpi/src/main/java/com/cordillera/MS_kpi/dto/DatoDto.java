package com.cordillera.MS_kpi.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DatoDto {
    private Long id;
    private String fuente;
    private String tipo;
    private BigDecimal valor;
    private String periodo;
    private String descripcion;
    private Boolean procesado;
    private LocalDateTime createdAt;
}
