package com.cordillera.MS_kpi.controller;

import com.cordillera.MS_kpi.dto.KpiDto;
import com.cordillera.MS_kpi.dto.KpiResponse;
import com.cordillera.MS_kpi.service.KpiService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class KpiControllerTest {

    @Mock KpiService kpiService;

    @InjectMocks KpiController controller;

    private final KpiResponse resp = KpiResponse.builder().id(1L).nombre("Ventas").build();

    @Test
    void calcular_devuelve201() {
        when(kpiService.calcular(any())).thenReturn(resp);
        assertThat(controller.calcular(new KpiDto()).getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void calcularDesdeDatos_devuelve201() {
        when(kpiService.calcularDesdeDatos(any(), any(), any(), any(), any())).thenReturn(resp);
        assertThat(controller.calcularDesdeDatos("SUMA", "VENTA", "2026-05", "Ventas", null).getStatusCode())
                .isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void listar_devuelve200() {
        when(kpiService.listarTodos()).thenReturn(List.of(resp));
        assertThat(controller.listar().getBody()).hasSize(1);
    }

    @Test
    void buscarPorId_devuelve200() {
        when(kpiService.buscarPorId(1L)).thenReturn(resp);
        assertThat(controller.buscarPorId(1L).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void listarPorPeriodo_devuelve200() {
        when(kpiService.listarPorPeriodo("2026-05")).thenReturn(List.of(resp));
        assertThat(controller.listarPorPeriodo("2026-05").getBody()).hasSize(1);
    }

    @Test
    void listarPorTipo_devuelve200() {
        when(kpiService.listarPorTipo("SUMA")).thenReturn(List.of(resp));
        assertThat(controller.listarPorTipo("SUMA").getBody()).hasSize(1);
    }

    @Test
    void actualizar_devuelve200() {
        when(kpiService.actualizar(eq(1L), any(), any())).thenReturn(resp);
        assertThat(controller.actualizar(1L, Map.of("nombre", "X", "descripcion", "Y")).getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    void eliminar_devuelve204() {
        assertThat(controller.eliminar(1L).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(kpiService).eliminar(1L);
    }

    @Test
    void health_ok() {
        assertThat(controller.health().getBody()).contains("OK");
    }
}
