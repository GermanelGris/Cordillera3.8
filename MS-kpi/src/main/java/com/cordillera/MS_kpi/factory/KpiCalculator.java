package com.cordillera.MS_kpi.factory;

import java.math.BigDecimal;
import java.util.List;

/**
 * Product – interfaz que define la operación de cálculo de KPI.
 */
public interface KpiCalculator {

    BigDecimal calcular(List<BigDecimal> valores);

    TipoCalculo getTipo();
}
