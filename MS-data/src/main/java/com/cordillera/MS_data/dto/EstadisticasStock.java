package com.cordillera.MS_data.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Estadísticas calculadas sobre el stock del inventario para el reporte.
 */
@Data
@Builder
public class EstadisticasStock {

    /** Cantidad de productos considerados. */
    private long cantidadProductos;
    /** Suma total de stock. */
    private long sumaStock;
    /** Promedio (media aritmética) del stock. */
    private double promedioStock;
    /** Mediana del stock. */
    private double medianaStock;
    /** Stock máximo. */
    private int stockMaximo;
    /** Stock mínimo. */
    private int stockMinimo;
}
