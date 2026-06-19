package com.cordillera.MS_reportes.controller;

import com.cordillera.MS_reportes.dto.ReporteDto;
import com.cordillera.MS_reportes.dto.ReporteResponse;
import com.cordillera.MS_reportes.service.ReporteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    @PostMapping
    public ResponseEntity<ReporteResponse> generar(@Valid @RequestBody ReporteDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reporteService.generar(dto));
    }

    @GetMapping
    public ResponseEntity<List<ReporteResponse>> listar() {
        return ResponseEntity.ok(reporteService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReporteResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(reporteService.buscarPorId(id));
    }

    @GetMapping("/periodo/{periodo}")
    public ResponseEntity<List<ReporteResponse>> listarPorPeriodo(@PathVariable String periodo) {
        return ResponseEntity.ok(reporteService.listarPorPeriodo(periodo));
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<ReporteResponse>> listarPorTipo(@PathVariable String tipo) {
        return ResponseEntity.ok(reporteService.listarPorTipo(tipo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReporteResponse> actualizar(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(reporteService.actualizar(id, body.get("titulo")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        reporteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("MS-Reportes OK");
    }
}
