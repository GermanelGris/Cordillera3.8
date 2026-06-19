package com.cordillera.MS_reportes.service;

import com.cordillera.MS_reportes.client.DatoClient;
import com.cordillera.MS_reportes.client.KpiClient;
import com.cordillera.MS_reportes.dto.DatoDto;
import com.cordillera.MS_reportes.dto.KpiDto;
import com.cordillera.MS_reportes.dto.ReporteDto;
import com.cordillera.MS_reportes.dto.ReporteResponse;
import com.cordillera.MS_reportes.entity.Reporte;
import com.cordillera.MS_reportes.factory.ReporteCreador;
import com.cordillera.MS_reportes.factory.ReporteCreadorRegistry;
import com.cordillera.MS_reportes.kafka.ReporteEventPublisher;
import com.cordillera.MS_reportes.repository.ReporteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private final ReporteRepository reporteRepository;
    private final ReporteCreadorRegistry creadorRegistry;
    private final ReporteEventPublisher eventPublisher;
    private final KpiClient kpiClient;
    private final DatoClient datoClient;

    @Transactional
    public ReporteResponse generar(ReporteDto dto) {
        // Obtiene datos y KPIs via Feign (con Circuit Breaker en los clients)
        List<KpiDto> kpis   = kpiClient.listarPorPeriodo(dto.getPeriodo());
        List<DatoDto> datos = datoClient.listarPorPeriodo(dto.getPeriodo());

        // Patrón Factory Method: obtiene el Creator según el tipo de reporte
        ReporteCreador creator = creadorRegistry.obtenerCreador(dto.getTipo());

        // Construye la entidad Reporte usando el Factory Method
        Reporte reporte = creator.construirReporte(dto, kpis, datos);

        reporte = reporteRepository.save(reporte);
        eventPublisher.publicarReporte(reporte);

        return toResponse(reporte);
    }

    public List<ReporteResponse> listarTodos() {
        return reporteRepository.findAll().stream().map(this::toResponse).toList();
    }

    public ReporteResponse buscarPorId(Long id) {
        return reporteRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Reporte no encontrado con id: " + id));
    }

    public List<ReporteResponse> listarPorPeriodo(String periodo) {
        return reporteRepository.findByPeriodo(periodo).stream().map(this::toResponse).toList();
    }

    public List<ReporteResponse> listarPorTipo(String tipo) {
        return reporteRepository.findByTipo(tipo.toUpperCase()).stream().map(this::toResponse).toList();
    }

    @Transactional
    public ReporteResponse actualizar(Long id, String titulo) {
        Reporte reporte = reporteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reporte no encontrado con id: " + id));
        if (titulo != null && !titulo.isBlank()) reporte.setTitulo(titulo);
        return toResponse(reporteRepository.save(reporte));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!reporteRepository.existsById(id)) {
            throw new RuntimeException("Reporte no encontrado con id: " + id);
        }
        reporteRepository.deleteById(id);
    }

    private ReporteResponse toResponse(Reporte r) {
        return ReporteResponse.builder()
                .id(r.getId())
                .tipo(r.getTipo())
                .titulo(r.getTitulo())
                .contenido(r.getContenido())
                .periodo(r.getPeriodo())
                .estado(r.getEstado())
                .generadoPor(r.getGeneradoPor())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
