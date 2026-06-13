package com.cordillera.MS_data.service;

import com.cordillera.MS_data.dto.InventarioRequest;
import com.cordillera.MS_data.entity.Inventario;
import com.cordillera.MS_data.repository.InventarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventarioService {

    private final InventarioRepository inventarioRepository;

    public List<Inventario> listarTodos() {
        return inventarioRepository.findAll();
    }

    public Inventario buscarPorProducto(Integer productoId) {
        return inventarioRepository.findByProductoId(productoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Producto " + productoId + " no encontrado en inventario"));
    }

    public Inventario buscarPorId(Long id) {
        return inventarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Inventario " + id + " no encontrado"));
    }

    public Inventario crear(InventarioRequest request) {
        if (inventarioRepository.findByProductoId(request.getProductoId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un producto con ID " + request.getProductoId());
        }
        Inventario inventario = Inventario.builder()
                .productoId(request.getProductoId())
                .nombre(request.getNombre())
                .stock(request.getStock())
                .build();
        return inventarioRepository.save(inventario);
    }

    public Inventario actualizar(Long id, InventarioRequest request) {
        Inventario inventario = buscarPorId(id);

        // producto_id es único: validar solo si cambió
        if (!inventario.getProductoId().equals(request.getProductoId())) {
            inventarioRepository.findByProductoId(request.getProductoId())
                    .filter(otro -> !otro.getId().equals(id))
                    .ifPresent(otro -> {
                        throw new ResponseStatusException(HttpStatus.CONFLICT,
                                "Ya existe un producto con ID " + request.getProductoId());
                    });
        }

        inventario.setProductoId(request.getProductoId());
        inventario.setNombre(request.getNombre());
        inventario.setStock(request.getStock());
        return inventarioRepository.save(inventario);
    }

    public void eliminar(Long id) {
        Inventario inventario = buscarPorId(id);
        inventarioRepository.delete(inventario);
    }

    @Transactional
    public Inventario descontar(Integer productoId, Integer cantidad) {
        int filas = inventarioRepository.descontarStock(productoId, cantidad);
        if (filas == 0) {
            Inventario inv = inventarioRepository.findByProductoId(productoId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Producto " + productoId + " no encontrado en inventario"));
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Stock insuficiente para '" + inv.getNombre() + "'. Disponible: " + inv.getStock());
        }
        return inventarioRepository.findByProductoId(productoId).orElseThrow();
    }
}
