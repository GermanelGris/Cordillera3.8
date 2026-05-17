package com.cordillera.MS_data.controller;

import com.cordillera.MS_data.dto.InventarioDescontarDto;
import com.cordillera.MS_data.entity.Inventario;
import com.cordillera.MS_data.service.InventarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final InventarioService inventarioService;

    @GetMapping
    public List<Inventario> listar() {
        return inventarioService.listarTodos();
    }

    @GetMapping("/producto/{productoId}")
    public Inventario buscar(@PathVariable Integer productoId) {
        return inventarioService.buscarPorProducto(productoId);
    }

    @PostMapping("/descontar")
    public ResponseEntity<Inventario> descontar(@Valid @RequestBody InventarioDescontarDto dto) {
        Inventario actualizado = inventarioService.descontar(dto.getProductoId(), dto.getCantidad());
        return ResponseEntity.ok(actualizado);
    }
}
