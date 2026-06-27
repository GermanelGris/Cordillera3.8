package com.cordillera.MS_data.controller;

import com.cordillera.MS_data.dto.DatoDto;
import com.cordillera.MS_data.dto.DatoResponse;
import com.cordillera.MS_data.service.DatoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class DatoControllerTest {

    @Mock DatoService datoService;

    @InjectMocks DatoController controller;

    private final DatoResponse resp = DatoResponse.builder().id(1L).tipo("VENTA").build();

    @Test
    void registrar_devuelve201() {
        when(datoService.registrar(any())).thenReturn(resp);
        assertThat(controller.registrar(new DatoDto()).getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void listar_devuelve200() {
        when(datoService.listarTodos()).thenReturn(List.of(resp));
        assertThat(controller.listar().getBody()).hasSize(1);
    }

    @Test
    void buscarPorId_devuelve200() {
        when(datoService.buscarPorId(1L)).thenReturn(resp);
        assertThat(controller.buscarPorId(1L).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void listarPorPeriodo_devuelve200() {
        when(datoService.listarPorPeriodo("2026-05")).thenReturn(List.of(resp));
        assertThat(controller.listarPorPeriodo("2026-05").getBody()).hasSize(1);
    }

    @Test
    void listarPorTipo_devuelve200() {
        when(datoService.listarPorTipo("VENTA")).thenReturn(List.of(resp));
        assertThat(controller.listarPorTipo("VENTA").getBody()).hasSize(1);
    }

    @Test
    void marcarProcesado_devuelve204() {
        assertThat(controller.marcarProcesado(1L).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(datoService).marcarProcesado(1L);
    }

    @Test
    void health_ok() {
        assertThat(controller.health().getBody()).contains("OK");
    }
}
