package com.cordillera.MS_kpi.service;

import com.cordillera.MS_kpi.client.DatoClient;
import com.cordillera.MS_kpi.dto.KpiDto;
import com.cordillera.MS_kpi.dto.KpiResponse;
import com.cordillera.MS_kpi.entity.Kpi;
import com.cordillera.MS_kpi.factory.KpiCalculatorCreator;
import com.cordillera.MS_kpi.factory.KpiCalculatorRegistry;
import com.cordillera.MS_kpi.kafka.KpiCorreoPublisher;
import com.cordillera.MS_kpi.kafka.KpiEventPublisher;
import com.cordillera.MS_kpi.repository.KpiRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class KpiServiceExtraTest {

    @Mock KpiRepository         kpiRepository;
    @Mock KpiCalculatorRegistry calculatorRegistry;
    @Mock KpiEventPublisher     eventPublisher;
    @Mock KpiCorreoPublisher    correoPublisher;
    @Mock DatoClient            datoClient;

    @InjectMocks KpiService kpiService;

    private Kpi kpi;

    @BeforeEach
    void setUp() {
        kpi = Kpi.builder().id(1L).nombre("Ventas").tipoCalculo("SUMA")
                .valor(new BigDecimal("150000")).periodo("2026-05").unidad("CLP")
                .createdAt(LocalDateTime.now()).build();
    }

    @Test
    void calcular_conDestinatario_publicaPorCorreo() {
        KpiDto dto = new KpiDto();
        dto.setNombre("Ventas");
        dto.setTipoCalculo("SUMA");
        dto.setValores(List.of(new BigDecimal("100000"), new BigDecimal("50000")));
        dto.setPeriodo("2026-05");
        dto.setDestinatario("cliente@correo.cl");

        KpiCalculatorCreator creator = mock(KpiCalculatorCreator.class);
        when(calculatorRegistry.obtenerCreador("SUMA")).thenReturn(creator);
        when(creator.calcular(anyList())).thenReturn(new BigDecimal("150000"));
        when(kpiRepository.save(any(Kpi.class))).thenReturn(kpi);

        kpiService.calcular(dto);

        verify(correoPublisher).publicar(eq(kpi), eq("cliente@correo.cl"));
    }

    @Test
    void actualizar_existente_cambiaNombreYDescripcion() {
        when(kpiRepository.findById(1L)).thenReturn(Optional.of(kpi));
        when(kpiRepository.save(any(Kpi.class))).thenAnswer(i -> i.getArgument(0));

        KpiResponse r = kpiService.actualizar(1L, "Ventas Actualizadas", "nueva desc");

        assertThat(r.getNombre()).isEqualTo("Ventas Actualizadas");
        assertThat(r.getDescripcion()).isEqualTo("nueva desc");
    }

    @Test
    void actualizar_noExiste_lanzaExcepcion() {
        when(kpiRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> kpiService.actualizar(99L, "x", "y"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no encontrado");
    }

    @Test
    void eliminar_existente_ok() {
        when(kpiRepository.existsById(1L)).thenReturn(true);

        kpiService.eliminar(1L);

        verify(kpiRepository).deleteById(1L);
    }

    @Test
    void eliminar_noExiste_lanzaExcepcion() {
        when(kpiRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> kpiService.eliminar(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no encontrado");
    }
}
