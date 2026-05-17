package com.cordillera.MS_reportes.client;

import com.cordillera.MS_reportes.dto.KpiDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class KpiClientFallback implements KpiClient {

    @Override
    public List<KpiDto> listarTodos() {
        log.warn("Circuit Breaker activado: MS-KPI no disponible (listarTodos)");
        return Collections.emptyList();
    }

    @Override
    public List<KpiDto> listarPorPeriodo(String periodo) {
        log.warn("Circuit Breaker activado: MS-KPI no disponible (periodo: {})", periodo);
        return Collections.emptyList();
    }
}
