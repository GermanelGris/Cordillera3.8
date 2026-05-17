package com.cordillera.MS_reportes.factory;

import com.cordillera.MS_reportes.factory.impl.ReporteKpiCreador;
import com.cordillera.MS_reportes.factory.impl.ReporteMensualCreador;
import com.cordillera.MS_reportes.factory.impl.ReporteResumenCreador;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Registro que mapea cada TipoReporte a su ConcreteCreator correspondiente.
 * Punto único de acceso al Factory Method de reportes.
 */
@Component
public class ReporteCreadorRegistry {

    private final Map<TipoReporte, ReporteCreador> creadores = Map.of(
            TipoReporte.KPI,     new ReporteKpiCreador(),
            TipoReporte.MENSUAL, new ReporteMensualCreador(),
            TipoReporte.RESUMEN, new ReporteResumenCreador()
    );

    public ReporteCreador obtenerCreador(TipoReporte tipo) {
        ReporteCreador creator = creadores.get(tipo);
        if (creator == null) {
            throw new IllegalArgumentException("Tipo de reporte no soportado: " + tipo);
        }
        return creator;
    }

    public ReporteCreador obtenerCreador(String tipo) {
        try {
            return obtenerCreador(TipoReporte.valueOf(tipo.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Tipo de reporte inválido: '" + tipo + "'. Válidos: KPI, MENSUAL, RESUMEN");
        }
    }
}
