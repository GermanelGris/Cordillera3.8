package com.cordillera.MS_data.controller;

import com.cordillera.MS_data.dto.InventarioDescontarDto;
import com.cordillera.MS_data.dto.InventarioRequest;
import com.cordillera.MS_data.entity.Inventario;
import com.cordillera.MS_data.service.InventarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    @GetMapping("/{id}")
    public Inventario obtener(@PathVariable Long id) {
        return inventarioService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<Inventario> crear(@Valid @RequestBody InventarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventarioService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Inventario> actualizar(@PathVariable Long id,
                                                 @Valid @RequestBody InventarioRequest request) {
        return ResponseEntity.ok(inventarioService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        inventarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/descontar")
    public ResponseEntity<Inventario> descontar(@Valid @RequestBody InventarioDescontarDto dto) {
        Inventario actualizado = inventarioService.descontar(dto.getProductoId(), dto.getCantidad());
        return ResponseEntity.ok(actualizado);
    }
}
