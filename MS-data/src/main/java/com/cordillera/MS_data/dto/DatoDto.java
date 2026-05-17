package com.cordillera.MS_data.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DatoDto {

    @NotBlank(message = "La fuente es obligatoria")
    private String fuente;

    @NotBlank(message = "El tipo es obligatorio")
    private String tipo;

    @NotNull(message = "El valor es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El valor debe ser positivo")
    private BigDecimal valor;

    @NotBlank(message = "El periodo es obligatorio (formato YYYY-MM)")
    private String periodo;

    private String descripcion;
}
