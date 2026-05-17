package com.cordillera.MS_data.service;

import com.cordillera.MS_data.dto.DatoDto;
import com.cordillera.MS_data.dto.DatoResponse;
import com.cordillera.MS_data.entity.DatoIngresado;
import com.cordillera.MS_data.kafka.DatoEventPublisher;
import com.cordillera.MS_data.repository.DatoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DatoService {

    private final DatoRepository datoRepository;
    private final DatoEventPublisher eventPublisher;

    @Transactional
    public DatoResponse registrar(DatoDto dto) {
        DatoIngresado dato = DatoIngresado.builder()
                .fuente(dto.getFuente())
                .tipo(dto.getTipo().toUpperCase())
                .valor(dto.getValor())
                .periodo(dto.getPeriodo())
                .descripcion(dto.getDescripcion())
                .build();

        dato = datoRepository.save(dato);
        eventPublisher.publicarDato(dato);

        return toResponse(dato);
    }

    public List<DatoResponse> listarTodos() {
        return datoRepository.findAll().stream().map(this::toResponse).toList();
    }

    public DatoResponse buscarPorId(Long id) {
        return datoRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Dato no encontrado con id: " + id));
    }

    public List<DatoResponse> listarPorPeriodo(String periodo) {
        return datoRepository.findByPeriodo(periodo).stream().map(this::toResponse).toList();
    }

    public List<DatoResponse> listarPorTipo(String tipo) {
        return datoRepository.findByTipo(tipo.toUpperCase()).stream().map(this::toResponse).toList();
    }

    @Transactional
    public void marcarProcesado(Long id) {
        DatoIngresado dato = datoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dato no encontrado con id: " + id));
        dato.setProcesado(true);
        datoRepository.save(dato);
    }

    private DatoResponse toResponse(DatoIngresado d) {
        return DatoResponse.builder()
                .id(d.getId())
                .fuente(d.getFuente())
                .tipo(d.getTipo())
                .valor(d.getValor())
                .periodo(d.getPeriodo())
                .descripcion(d.getDescripcion())
                .procesado(d.getProcesado())
                .createdAt(d.getCreatedAt())
                .build();
    }
}
