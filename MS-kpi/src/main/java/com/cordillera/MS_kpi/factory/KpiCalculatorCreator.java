package com.cordillera.MS_kpi.factory;

import java.math.BigDecimal;
import java.util.List;

/**
 * Creator abstracto – define el Factory Method {@code crearCalculador()}.
 * Las subclases concretas deciden qué producto (KpiCalculator) instanciar.
 */
public abstract class KpiCalculatorCreator {

    /** Factory Method */
    public abstract KpiCalculator crearCalculador();

    /** Operación plantilla que usa el producto creado */
    public BigDecimal calcular(List<BigDecimal> valores) {
        if (valores == null || valores.isEmpty()) {
            throw new IllegalArgumentException("La lista de valores no puede estar vacía");
        }
        return crearCalculador().calcular(valores);
    }

    public TipoCalculo getTipo() {
        return crearCalculador().getTipo();
    }
}
