package com.cordillera.MS_kpi.factory.impl;

import com.cordillera.MS_kpi.factory.KpiCalculator;
import com.cordillera.MS_kpi.factory.TipoCalculo;

import java.math.BigDecimal;
import java.util.List;

/** ConcreteProduct – calcula la suma de los valores. */
public class SumaCalculator implements KpiCalculator {

    @Override
    public BigDecimal calcular(List<BigDecimal> valores) {
        return valores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public TipoCalculo getTipo() {
        return TipoCalculo.SUMA;
    }
}
