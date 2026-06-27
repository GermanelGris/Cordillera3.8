package com.cordillera.MS_data.service;

import com.cordillera.MS_data.dto.InventarioRequest;
import com.cordillera.MS_data.entity.Inventario;
import com.cordillera.MS_data.repository.InventarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {

    @Mock InventarioRepository inventarioRepository;

    @InjectMocks InventarioService service;

    private Inventario inv;

    @BeforeEach
    void setUp() {
        inv = Inventario.builder().id(1L).productoId(100).nombre("Producto A").stock(50).build();
    }

    private InventarioRequest request(int productoId, String nombre, int stock) {
        InventarioRequest r = new InventarioRequest();
        r.setProductoId(productoId);
        r.setNombre(nombre);
        r.setStock(stock);
        return r;
    }

    @Test
    void listarTodos_ok() {
        when(inventarioRepository.findAll()).thenReturn(List.of(inv));
        assertThat(service.listarTodos()).hasSize(1);
    }

    @Test
    void buscarPorProducto_existente_ok() {
        when(inventarioRepository.findByProductoId(100)).thenReturn(Optional.of(inv));
        assertThat(service.buscarPorProducto(100).getNombre()).isEqualTo("Producto A");
    }

    @Test
    void buscarPorProducto_noExiste_lanza404() {
        when(inventarioRepository.findByProductoId(999)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.buscarPorProducto(999))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("no encontrado");
    }

    @Test
    void crear_nuevo_ok() {
        when(inventarioRepository.findByProductoId(100)).thenReturn(Optional.empty());
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(i -> i.getArgument(0));

        Inventario creado = service.crear(request(100, "Producto A", 50));

        assertThat(creado.getProductoId()).isEqualTo(100);
    }

    @Test
    void crear_duplicado_lanza409() {
        when(inventarioRepository.findByProductoId(100)).thenReturn(Optional.of(inv));
        assertThatThrownBy(() -> service.crear(request(100, "X", 1)))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("Ya existe");
    }

    @Test
    void actualizar_mismoProductoId_ok() {
        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inv));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(i -> i.getArgument(0));

        Inventario r = service.actualizar(1L, request(100, "Nuevo Nombre", 80));

        assertThat(r.getNombre()).isEqualTo("Nuevo Nombre");
        assertThat(r.getStock()).isEqualTo(80);
    }

    @Test
    void actualizar_cambiaProductoIdAUnoLibre_ok() {
        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inv));
        when(inventarioRepository.findByProductoId(101)).thenReturn(Optional.empty());
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(i -> i.getArgument(0));

        Inventario r = service.actualizar(1L, request(101, "Producto A", 50));

        assertThat(r.getProductoId()).isEqualTo(101);
    }

    @Test
    void actualizar_cambiaProductoIdAUnoOcupado_lanza409() {
        Inventario otro = Inventario.builder().id(2L).productoId(101).nombre("Otro").stock(5).build();
        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inv));
        when(inventarioRepository.findByProductoId(101)).thenReturn(Optional.of(otro));

        assertThatThrownBy(() -> service.actualizar(1L, request(101, "X", 1)))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("Ya existe");
    }

    @Test
    void eliminar_ok() {
        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inv));
        service.eliminar(1L);
        verify(inventarioRepository).delete(inv);
    }

    @Test
    void descontar_conStock_ok() {
        when(inventarioRepository.descontarStock(100, 10)).thenReturn(1);
        when(inventarioRepository.findByProductoId(100)).thenReturn(Optional.of(inv));

        assertThat(service.descontar(100, 10)).isNotNull();
    }

    @Test
    void descontar_stockInsuficiente_lanza409() {
        when(inventarioRepository.descontarStock(100, 999)).thenReturn(0);
        when(inventarioRepository.findByProductoId(100)).thenReturn(Optional.of(inv));

        assertThatThrownBy(() -> service.descontar(100, 999))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("Stock insuficiente");
    }

    @Test
    void descontar_productoNoExiste_lanza404() {
        when(inventarioRepository.descontarStock(999, 1)).thenReturn(0);
        when(inventarioRepository.findByProductoId(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.descontar(999, 1))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("no encontrado");
    }
}
