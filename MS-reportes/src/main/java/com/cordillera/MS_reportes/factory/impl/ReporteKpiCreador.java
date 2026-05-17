package com.cordillera.MS_reportes.factory.impl;

import com.cordillera.MS_reportes.factory.ReporteContenido;
import com.cordillera.MS_reportes.factory.ReporteCreador;

/** ConcreteCreator – crea un ReporteKpiContenido. */
public class ReporteKpiCreador extends ReporteCreador {

    @Override
    public ReporteContenido crearContenido() {
        return new ReporteKpiContenido();
    }
}
