package com.cordillera.MS_kpi.factory;

import com.cordillera.MS_kpi.factory.impl.*;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Registro que mapea cada TipoCalculo a su ConcreteCreator correspondiente.
 * Punto único de acceso al Factory Method.
 */
@Component
public class KpiCalculatorRegistry {

    private final Map<TipoCalculo, KpiCalculatorCreator> creadores = Map.of(
            TipoCalculo.PROMEDIO, new PromedioCreator(),
            TipoCalculo.SUMA,     new SumaCreator(),
            TipoCalculo.MAXIMO,   new MaximoCreator(),
            TipoCalculo.MINIMO,   new MinimoCreator()
    );

    public KpiCalculatorCreator obtenerCreador(TipoCalculo tipo) {
        KpiCalculatorCreator creator = creadores.get(tipo);
        if (creator == null) {
            throw new IllegalArgumentException("Tipo de cálculo no soportado: " + tipo);
        }
        return creator;
    }

    public KpiCalculatorCreator obtenerCreador(String tipo) {
        try {
            return obtenerCreador(TipoCalculo.valueOf(tipo.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Tipo de cálculo inválido: '" + tipo + "'. Válidos: PROMEDIO, SUMA, MAXIMO, MINIMO");
        }
    }
}
