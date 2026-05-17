package com.cordillera.MS_reportes.factory;

import com.cordillera.MS_reportes.dto.DatoDto;
import com.cordillera.MS_reportes.dto.KpiDto;
import com.cordillera.MS_reportes.dto.ReporteDto;

import java.util.List;

/**
 * Product – interfaz que define cómo se construye el contenido de un reporte.
 */
public interface ReporteContenido {

    String construirContenido(ReporteDto dto, List<KpiDto> kpis, List<DatoDto> datos);

    TipoReporte getTipo();
}
