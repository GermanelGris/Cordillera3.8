package com.cordillera.MS_data.dto;

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
public class DatoResponse {
    private Long id;
    private String fuente;
    private String tipo;
    private BigDecimal valor;
    private String periodo;
    private String descripcion;
    private Boolean procesado;
    private LocalDateTime createdAt;
}
