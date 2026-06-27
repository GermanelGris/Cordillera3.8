package com.cordillera.MS_reportes.controller;

import com.cordillera.MS_reportes.dto.ReporteDto;
import com.cordillera.MS_reportes.dto.ReporteResponse;
import com.cordillera.MS_reportes.service.ReporteService;
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
class ReporteControllerTest {

    @Mock ReporteService reporteService;

    @InjectMocks ReporteController controller;

    private final ReporteResponse resp = ReporteResponse.builder().id(1L).titulo("R").build();

    @Test
    void generar_devuelve201() {
        when(reporteService.generar(any())).thenReturn(resp);
        assertThat(controller.generar(new ReporteDto()).getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void listar_devuelve200() {
        when(reporteService.listarTodos()).thenReturn(List.of(resp));
        assertThat(controller.listar().getBody()).hasSize(1);
    }

    @Test
    void buscarPorId_devuelve200() {
        when(reporteService.buscarPorId(1L)).thenReturn(resp);
        assertThat(controller.buscarPorId(1L).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void listarPorPeriodo_devuelve200() {
        when(reporteService.listarPorPeriodo("2026-05")).thenReturn(List.of(resp));
        assertThat(controller.listarPorPeriodo("2026-05").getBody()).hasSize(1);
    }

    @Test
    void listarPorTipo_devuelve200() {
        when(reporteService.listarPorTipo("KPI")).thenReturn(List.of(resp));
        assertThat(controller.listarPorTipo("KPI").getBody()).hasSize(1);
    }

    @Test
    void actualizar_devuelve200() {
        when(reporteService.actualizar(eq(1L), any())).thenReturn(resp);
        assertThat(controller.actualizar(1L, Map.of("titulo", "Nuevo")).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void eliminar_devuelve204() {
        assertThat(controller.eliminar(1L).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(reporteService).eliminar(1L);
    }

    @Test
    void health_ok() {
        assertThat(controller.health().getBody()).contains("OK");
    }
}
