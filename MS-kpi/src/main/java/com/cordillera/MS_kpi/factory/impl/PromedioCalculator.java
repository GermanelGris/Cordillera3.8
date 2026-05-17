package com.cordillera.MS_kpi.factory.impl;

import com.cordillera.MS_kpi.factory.KpiCalculator;
import com.cordillera.MS_kpi.factory.TipoCalculo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** ConcreteProduct – calcula el promedio de los valores. */
public class PromedioCalculator implements KpiCalculator {

    @Override
    public BigDecimal calcular(List<BigDecimal> valores) {
        BigDecimal suma = valores.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return suma.divide(BigDecimal.valueOf(valores.size()), 4, RoundingMode.HALF_UP);
    }

    @Override
    public TipoCalculo getTipo() {
        return TipoCalculo.PROMEDIO;
    }
}
