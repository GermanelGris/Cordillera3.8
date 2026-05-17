package com.cordillera.MS_data.service;

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
