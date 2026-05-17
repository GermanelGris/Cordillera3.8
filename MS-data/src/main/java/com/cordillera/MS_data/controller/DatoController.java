package com.cordillera.MS_data.controller;

import com.cordillera.MS_data.dto.DatoDto;
import com.cordillera.MS_data.dto.DatoResponse;
import com.cordillera.MS_data.service.DatoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/datos")
@RequiredArgsConstructor
public class DatoController {

    private final DatoService datoService;

    @PostMapping
    public ResponseEntity<DatoResponse> registrar(@Valid @RequestBody DatoDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(datoService.registrar(dto));
    }

    @GetMapping
    public ResponseEntity<List<DatoResponse>> listar() {
        return ResponseEntity.ok(datoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DatoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(datoService.buscarPorId(id));
    }

    @GetMapping("/periodo/{periodo}")
    public ResponseEntity<List<DatoResponse>> listarPorPeriodo(@PathVariable String periodo) {
        return ResponseEntity.ok(datoService.listarPorPeriodo(periodo));
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<DatoResponse>> listarPorTipo(@PathVariable String tipo) {
        return ResponseEntity.ok(datoService.listarPorTipo(tipo));
    }

    @PatchMapping("/{id}/procesado")
    public ResponseEntity<Void> marcarProcesado(@PathVariable Long id) {
        datoService.marcarProcesado(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("MS-Data OK");
    }
}
