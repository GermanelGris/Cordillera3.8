package com.cordillera.MS_kpi.factory.impl;

import com.cordillera.MS_kpi.factory.KpiCalculator;
import com.cordillera.MS_kpi.factory.TipoCalculo;

import java.math.BigDecimal;
import java.util.List;

/** ConcreteProduct – obtiene el valor máximo. */
public class MaximoCalculator implements KpiCalculator {

    @Override
    public BigDecimal calcular(List<BigDecimal> valores) {
        return valores.stream()
                .max(BigDecimal::compareTo)
                .orElseThrow(() -> new IllegalArgumentException("Lista vacía"));
    }

    @Override
    public TipoCalculo getTipo() {
        return TipoCalculo.MAXIMO;
    }
}
