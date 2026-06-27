package com.cordillera.MS_data.service;

import com.cordillera.MS_data.dto.EstadisticasStock;
import com.cordillera.MS_data.entity.Inventario;
import com.cordillera.MS_data.repository.InventarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EstadisticaStockServiceTest {

    @Mock InventarioRepository inventarioRepository;

    @InjectMocks EstadisticaStockService service;

    private Inventario inv(int productoId, int stock) {
        return Inventario.builder().productoId(productoId).nombre("P" + productoId).stock(stock).build();
    }

    @Test
    void calcular_sinInventario_retornaCeros() {
        when(inventarioRepository.findAll()).thenReturn(List.of());

        EstadisticasStock e = service.calcular();

        assertThat(e.getCantidadProductos()).isZero();
        assertThat(e.getSumaStock()).isZero();
    }

    @Test
    void calcular_cantidadImpar_medianaEsCentral() {
        when(inventarioRepository.findAll()).thenReturn(List.of(inv(1, 10), inv(2, 30), inv(3, 20)));

        EstadisticasStock e = service.calcular();

        assertThat(e.getCantidadProductos()).isEqualTo(3);
        assertThat(e.getSumaStock()).isEqualTo(60);
        assertThat(e.getPromedioStock()).isEqualTo(20.0);
        assertThat(e.getMedianaStock()).isEqualTo(20.0); // ordenado: 10,20,30
        assertThat(e.getStockMaximo()).isEqualTo(30);
        assertThat(e.getStockMinimo()).isEqualTo(10);
    }

    @Test
    void calcular_cantidadPar_medianaEsPromedioDeCentrales() {
        when(inventarioRepository.findAll()).thenReturn(List.of(inv(1, 10), inv(2, 20), inv(3, 30), inv(4, 40)));

        EstadisticasStock e = service.calcular();

        assertThat(e.getMedianaStock()).isEqualTo(25.0); // (20+30)/2
    }

    @Test
    void listarInventario_devuelveTodos() {
        when(inventarioRepository.findAll()).thenReturn(List.of(inv(1, 10)));

        assertThat(service.listarInventario()).hasSize(1);
    }
}
