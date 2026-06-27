package com.cordillera.MS_data.controller;

import com.cordillera.MS_data.dto.InventarioDescontarDto;
import com.cordillera.MS_data.dto.InventarioRequest;
import com.cordillera.MS_data.entity.Inventario;
import com.cordillera.MS_data.service.InventarioService;
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
class InventarioControllerTest {

    @Mock InventarioService inventarioService;

    @InjectMocks InventarioController controller;

    private final Inventario inv = Inventario.builder().id(1L).productoId(100).nombre("P").stock(10).build();

    @Test
    void listar_ok() {
        when(inventarioService.listarTodos()).thenReturn(List.of(inv));
        assertThat(controller.listar()).hasSize(1);
    }

    @Test
    void buscarPorProducto_ok() {
        when(inventarioService.buscarPorProducto(100)).thenReturn(inv);
        assertThat(controller.buscar(100).getProductoId()).isEqualTo(100);
    }

    @Test
    void obtenerPorId_ok() {
        when(inventarioService.buscarPorId(1L)).thenReturn(inv);
        assertThat(controller.obtener(1L).getId()).isEqualTo(1L);
    }

    @Test
    void crear_devuelve201() {
        when(inventarioService.crear(any())).thenReturn(inv);
        assertThat(controller.crear(new InventarioRequest()).getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void actualizar_devuelve200() {
        when(inventarioService.actualizar(eq(1L), any())).thenReturn(inv);
        assertThat(controller.actualizar(1L, new InventarioRequest()).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void eliminar_devuelve204() {
        assertThat(controller.eliminar(1L).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(inventarioService).eliminar(1L);
    }

    @Test
    void descontar_devuelve200() {
        InventarioDescontarDto dto = new InventarioDescontarDto();
        dto.setProductoId(100);
        dto.setCantidad(5);
        when(inventarioService.descontar(100, 5)).thenReturn(inv);

        assertThat(controller.descontar(dto).getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
