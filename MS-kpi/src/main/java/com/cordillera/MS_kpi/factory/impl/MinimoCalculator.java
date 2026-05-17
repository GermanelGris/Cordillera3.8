package com.cordillera.MS_kpi.factory.impl;

import com.cordillera.MS_kpi.factory.KpiCalculator;
import com.cordillera.MS_kpi.factory.TipoCalculo;

import java.math.BigDecimal;
import java.util.List;

/** ConcreteProduct – obtiene el valor mínimo. */
public class MinimoCalculator implements KpiCalculator {

    @Override
    public BigDecimal calcular(List<BigDecimal> valores) {
        return valores.stream()
                .min(BigDecimal::compareTo)
                .orElseThrow(() -> new IllegalArgumentException("Lista vacía"));
    }

    @Override
    public TipoCalculo getTipo() {
        return TipoCalculo.MINIMO;
    }
}
