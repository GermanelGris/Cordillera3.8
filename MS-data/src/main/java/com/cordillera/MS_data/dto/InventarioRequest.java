package com.cordillera.MS_data.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InventarioRequest {

    @NotNull(message = "El ID de producto es obligatorio")
    @Min(value = 1, message = "El ID de producto debe ser mayor a 0")
    private Integer productoId;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 200)
    private String nombre;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;
}
