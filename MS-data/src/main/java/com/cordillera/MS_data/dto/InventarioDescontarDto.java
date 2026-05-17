package com.cordillera.MS_data.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InventarioDescontarDto {

    @NotNull
    private Integer productoId;

    @NotNull
    @Min(1)
    private Integer cantidad;
}
