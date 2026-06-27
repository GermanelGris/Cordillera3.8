package com.cordillera.MS_kpi.client;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class DatoClientFallbackTest {

    private final DatoClientFallback fallback = new DatoClientFallback();

    @Test
    void todosLosMetodos_retornanListaVacia() {
        assertThat(fallback.listarTodos()).isEmpty();
        assertThat(fallback.listarPorPeriodo("2026-05")).isEmpty();
        assertThat(fallback.listarPorTipo("VENTA")).isEmpty();
    }
}
