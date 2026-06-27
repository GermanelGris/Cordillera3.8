package com.cordillera.MS_reportes.service;

import com.cordillera.MS_reportes.client.DatoClient;
import com.cordillera.MS_reportes.client.KpiClient;
import com.cordillera.MS_reportes.dto.ReporteDto;
import com.cordillera.MS_reportes.dto.ReporteResponse;
import com.cordillera.MS_reportes.entity.Reporte;
import com.cordillera.MS_reportes.factory.ReporteCreador;
import com.cordillera.MS_reportes.factory.ReporteCreadorRegistry;
import com.cordillera.MS_reportes.kafka.ReporteCorreoPublisher;
import com.cordillera.MS_reportes.kafka.ReporteEventPublisher;
import com.cordillera.MS_reportes.repository.ReporteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class ReporteServiceExtraTest {

    @Mock ReporteRepository      reporteRepository;
    @Mock ReporteCreadorRegistry creadorRegistry;
    @Mock ReporteEventPublisher  eventPublisher;
    @Mock ReporteCorreoPublisher correoPublisher;
    @Mock KpiClient              kpiClient;
    @Mock DatoClient             datoClient;

    @InjectMocks ReporteService reporteService;

    private Reporte reporte;

    @BeforeEach
    void setUp() {
        reporte = Reporte.builder().id(1L).tipo("KPI").titulo("R").contenido("c")
                .periodo("2026-05").estado("GENERADO").generadoPor("admin")
                .createdAt(LocalDateTime.now()).build();
    }

    @Test
    void generar_conDestinatario_publicaCorreo() {
        ReporteDto dto = new ReporteDto();
        dto.setTipo("KPI");
        dto.setTitulo("R");
        dto.setPeriodo("2026-05");
        dto.setDestinatario("cliente@correo.cl");

        ReporteCreador creador = mock(ReporteCreador.class);
        when(kpiClient.listarPorPeriodo("2026-05")).thenReturn(List.of());
        when(datoClient.listarPorPeriodo("2026-05")).thenReturn(List.of());
        when(creadorRegistry.obtenerCreador("KPI")).thenReturn(creador);
        when(creador.construirReporte(eq(dto), anyList(), anyList())).thenReturn(reporte);
        when(reporteRepository.save(reporte)).thenReturn(reporte);

        reporteService.generar(dto);

        verify(correoPublisher).publicar(eq(reporte), eq("cliente@correo.cl"));
    }

    @Test
    void actualizar_existente_cambiaTitulo() {
        when(reporteRepository.findById(1L)).thenReturn(Optional.of(reporte));
        when(reporteRepository.save(any(Reporte.class))).thenAnswer(i -> i.getArgument(0));

        ReporteResponse r = reporteService.actualizar(1L, "Nuevo Titulo");

        assertThat(r.getTitulo()).isEqualTo("Nuevo Titulo");
    }

    @Test
    void actualizar_noExiste_lanzaExcepcion() {
        when(reporteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reporteService.actualizar(99L, "x"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no encontrado");
    }

    @Test
    void eliminar_existente_ok() {
        when(reporteRepository.existsById(1L)).thenReturn(true);

        reporteService.eliminar(1L);

        verify(reporteRepository).deleteById(1L);
    }

    @Test
    void eliminar_noExiste_lanzaExcepcion() {
        when(reporteRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> reporteService.eliminar(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no encontrado");
    }
}
