package com.cordillera.MS_reportes.factory.impl;

import com.cordillera.MS_reportes.factory.ReporteContenido;
import com.cordillera.MS_reportes.factory.ReporteCreador;

/** ConcreteCreador – crea un ReporteMensualContenido. */
public class ReporteMensualCreador extends ReporteCreador {

    @Override
    public ReporteContenido crearContenido() {
        return new ReporteMensualContenido();
    }
}
