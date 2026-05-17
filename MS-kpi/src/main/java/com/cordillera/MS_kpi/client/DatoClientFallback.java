package com.cordillera.MS_kpi.client;

import com.cordillera.MS_kpi.dto.DatoDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class DatoClientFallback implements DatoClient {

    @Override
    public List<DatoDto> listarTodos() {
        log.warn("Circuit Breaker activado: MS-Data no disponible (listarTodos)");
        return Collections.emptyList();
    }

    @Override
    public List<DatoDto> listarPorPeriodo(String periodo) {
        log.warn("Circuit Breaker activado: MS-Data no disponible (periodo: {})", periodo);
        return Collections.emptyList();
    }

    @Override
    public List<DatoDto> listarPorTipo(String tipo) {
        log.warn("Circuit Breaker activado: MS-Data no disponible (tipo: {})", tipo);
        return Collections.emptyList();
    }
}
