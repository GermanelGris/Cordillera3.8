package com.cordillera.MS_kpi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KpiResponse {
    private Long id;
    private String nombre;
    private String tipoCalculo;
    private BigDecimal valor;
    private String periodo;
    private String unidad;
    private String descripcion;
    private LocalDateTime createdAt;
}
