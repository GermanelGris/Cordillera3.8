package com.cordillera.MS_reportes.factory;

import com.cordillera.MS_reportes.dto.DatoDto;
import com.cordillera.MS_reportes.dto.KpiDto;
import com.cordillera.MS_reportes.dto.ReporteDto;
import com.cordillera.MS_reportes.entity.Reporte;

import java.util.List;

/**
 * Creator abstracto – define el Factory Method {@code crearContenido()}.
 * Cada subclase concreta decide qué ReporteContenido (Product) instanciar.
 */
public abstract class ReporteCreador {

    /** Factory Method */
    public abstract ReporteContenido crearContenido();

    /** Operación plantilla que construye la entidad Reporte usando el producto */
    public Reporte construirReporte(ReporteDto dto, List<KpiDto> kpis, List<DatoDto> datos) {
        ReporteContenido contenidoBuilder = crearContenido();
        String contenido = contenidoBuilder.construirContenido(dto, kpis, datos);

        return Reporte.builder()
                .tipo(crearContenido().getTipo().name())
                .titulo(dto.getTitulo())
                .contenido(contenido)
                .periodo(dto.getPeriodo())
                .estado("GENERADO")
                .generadoPor(dto.getGeneradoPor() != null ? dto.getGeneradoPor() : "sistema")
                .build();
    }
}
