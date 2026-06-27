package com.cordillera.MS_reportes.client;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ClientFallbackTest {

    @Test
    void datoClientFallback_retornaListasVacias() {
        DatoClientFallback fb = new DatoClientFallback();
        assertThat(fb.listarTodos()).isEmpty();
        assertThat(fb.listarPorPeriodo("2026-05")).isEmpty();
    }

    @Test
    void kpiClientFallback_retornaListasVacias() {
        KpiClientFallback fb = new KpiClientFallback();
        assertThat(fb.listarTodos()).isEmpty();
        assertThat(fb.listarPorPeriodo("2026-05")).isEmpty();
    }
}
