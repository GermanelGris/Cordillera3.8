package com.cordillera.MS_reportes.factory.impl;

import com.cordillera.MS_reportes.factory.ReporteContenido;
import com.cordillera.MS_reportes.factory.ReporteCreador;

/** ConcreteCreador – crea un ReporteResumenContenido. */
public class ReporteResumenCreador extends ReporteCreador {

    @Override
    public ReporteContenido crearContenido() {
        return new ReporteResumenContenido();
    }
}
