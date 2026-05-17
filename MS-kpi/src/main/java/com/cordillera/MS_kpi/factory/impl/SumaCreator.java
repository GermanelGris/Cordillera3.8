package com.cordillera.MS_kpi.factory.impl;

import com.cordillera.MS_kpi.factory.KpiCalculator;
import com.cordillera.MS_kpi.factory.KpiCalculatorCreator;

/** ConcreteCreator – crea un SumaCalculator. */
public class SumaCreator extends KpiCalculatorCreator {

    @Override
    public KpiCalculator crearCalculador() {
        return new SumaCalculator();
    }
}
