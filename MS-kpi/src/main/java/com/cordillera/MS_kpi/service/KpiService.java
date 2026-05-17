package com.cordillera.MS_kpi.service;

import com.cordillera.MS_kpi.client.DatoClient;
import com.cordillera.MS_kpi.dto.DatoDto;
import com.cordillera.MS_kpi.dto.KpiDto;
import com.cordillera.MS_kpi.dto.KpiResponse;
import com.cordillera.MS_kpi.entity.Kpi;
import com.cordillera.MS_kpi.factory.KpiCalculatorCreator;
import com.cordillera.MS_kpi.factory.KpiCalculatorRegistry;
import com.cordillera.MS_kpi.kafka.KpiEventPublisher;
import com.cordillera.MS_kpi.repository.KpiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KpiService {

    private final KpiRepository kpiRepository;
    private final KpiCalculatorRegistry calculatorRegistry;
    private final KpiEventPublisher eventPublisher;
    private final DatoClient datoClient;

    @Transactional
    public KpiResponse calcular(KpiDto dto) {
        // Patrón Factory Method: obtiene el Creator según el tipo
        KpiCalculatorCreator creator = calculatorRegistry.obtenerCreador(dto.getTipoCalculo());

        // Delega el cálculo al producto creado por el Creator
        BigDecimal resultado = creator.calcular(dto.getValores());

        Kpi kpi = Kpi.builder()
                .nombre(dto.getNombre())
                .tipoCalculo(dto.getTipoCalculo().toUpperCase())
                .valor(resultado)
                .periodo(dto.getPeriodo())
                .unidad(dto.getUnidad() != null ? dto.getUnidad() : "CLP")
                .descripcion(dto.getDescripcion())
                .build();

        kpi = kpiRepository.save(kpi);
        eventPublisher.publicarKpi(kpi);

        return toResponse(kpi);
    }

    @Transactional
    public KpiResponse calcularDesdeDatos(String tipoCalculo, String tipoDato, String periodo, String nombre) {
        List<DatoDto> datos = datoClient.listarPorPeriodo(periodo);

        List<BigDecimal> valores = datos.stream()
                .filter(d -> tipoDato.equalsIgnoreCase(d.getTipo()))
                .map(DatoDto::getValor)
                .toList();

        if (valores.isEmpty()) {
            throw new RuntimeException("No se encontraron datos de tipo '" + tipoDato + "' para el periodo " + periodo);
        }

        KpiDto dto = new KpiDto();
        dto.setNombre(nombre);
        dto.setTipoCalculo(tipoCalculo);
        dto.setValores(valores);
        dto.setPeriodo(periodo);

        return calcular(dto);
    }

    public List<KpiResponse> listarTodos() {
        return kpiRepository.findAll().stream().map(this::toResponse).toList();
    }

    public KpiResponse buscarPorId(Long id) {
        return kpiRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("KPI no encontrado con id: " + id));
    }

    public List<KpiResponse> listarPorPeriodo(String periodo) {
        return kpiRepository.findByPeriodo(periodo).stream().map(this::toResponse).toList();
    }

    public List<KpiResponse> listarPorTipo(String tipoCalculo) {
        return kpiRepository.findByTipoCalculo(tipoCalculo.toUpperCase()).stream().map(this::toResponse).toList();
    }

    private KpiResponse toResponse(Kpi k) {
        return KpiResponse.builder()
                .id(k.getId())
                .nombre(k.getNombre())
                .tipoCalculo(k.getTipoCalculo())
                .valor(k.getValor())
                .periodo(k.getPeriodo())
                .unidad(k.getUnidad())
                .descripcion(k.getDescripcion())
                .createdAt(k.getCreatedAt())
                .build();
    }
}
