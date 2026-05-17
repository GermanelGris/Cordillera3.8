package com.cordillera.MS_kpi.factory.impl;

import com.cordillera.MS_kpi.factory.KpiCalculator;
import com.cordillera.MS_kpi.factory.KpiCalculatorCreator;

/** ConcreteCreator – crea un MinimoCalculator. */
public class MinimoCreator extends KpiCalculatorCreator {

    @Override
    public KpiCalculator crearCalculador() {
        return new MinimoCalculator();
    }
}
