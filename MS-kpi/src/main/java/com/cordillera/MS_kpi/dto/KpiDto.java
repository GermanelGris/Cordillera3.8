package com.cordillera.MS_kpi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class KpiDto {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El tipo de cálculo es obligatorio (PROMEDIO, SUMA, MAXIMO, MINIMO)")
    private String tipoCalculo;

    @NotEmpty(message = "Se requiere al menos un valor para calcular")
    private List<BigDecimal> valores;

    @NotBlank(message = "El periodo es obligatorio")
    private String periodo;

    private String unidad;
    private String descripcion;
}
